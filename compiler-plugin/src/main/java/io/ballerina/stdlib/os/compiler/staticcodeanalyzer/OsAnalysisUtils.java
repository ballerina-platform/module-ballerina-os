/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.os.compiler.staticcodeanalyzer;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.BALLERINA_ORG;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.OS;

/**
 * Shared helpers for the OS static code analysis rules.
 */
public final class OsAnalysisUtils {

    private static final String PUBLIC_QUALIFIER = "public";

    private OsAnalysisUtils() {
    }

    /**
     * Retrieve the document being analyzed.
     *
     * @param context the syntax node analysis context
     * @return the document the analyzed node belongs to
     */
    public static Document getDocument(SyntaxNodeAnalysisContext context) {
        return context.currentPackage().module(context.moduleId()).document(context.documentId());
    }

    /**
     * Collect every prefix the {@code ballerina/os} module is imported under in the document being analyzed.
     *
     * @param context the syntax node analysis context
     * @return the prefixes the OS module is reachable through in this document
     */
    public static Set<String> collectOsPrefixes(SyntaxNodeAnalysisContext context) {
        Set<String> prefixes = new HashSet<>();
        if (!(getDocument(context).syntaxTree().rootNode() instanceof ModulePartNode modulePart)) {
            return prefixes;
        }
        for (ImportDeclarationNode importDeclaration : modulePart.imports()) {
            Optional<ImportOrgNameNode> orgName = importDeclaration.orgName();
            boolean isOsImport = orgName.isPresent() && BALLERINA_ORG.equals(orgName.get().orgName().text())
                    && importDeclaration.moduleName().stream().anyMatch(name -> OS.equals(name.text()));
            if (isOsImport) {
                prefixes.add(importDeclaration.prefix().map(prefix -> prefix.prefix().text()).orElse(OS));
            }
        }
        return prefixes;
    }

    /**
     * Get the name of the OS module function being called, if the call targets the OS module at all.
     *
     * @param functionCall  the call to inspect
     * @param osPrefixes    the prefixes the OS module is reachable through
     * @param semanticModel the semantic model, used to confirm the prefix resolves to the imported module
     * @return the function's simple name if the call targets the OS module, empty otherwise
     */
    public static Optional<String> getOsFunctionName(FunctionCallExpressionNode functionCall, Set<String> osPrefixes,
                                                     SemanticModel semanticModel) {
        if (!(functionCall.functionName() instanceof QualifiedNameReferenceNode qualifiedName)) {
            return Optional.empty();
        }
        Optional<Symbol> modulePrefix = semanticModel.symbol(qualifiedName.modulePrefix());
        Optional<Symbol> function = semanticModel.symbol(qualifiedName.identifier());
        if (modulePrefix.isEmpty() || function.isEmpty()
                || modulePrefix.get().getName().isEmpty() || function.get().getName().isEmpty()) {
            return Optional.empty();
        }
        if (!osPrefixes.contains(modulePrefix.get().getName().get())) {
            return Optional.empty();
        }
        return function.get().getName();
    }

    /**
     * Get an argument by position or by name, so a rule reads the same value whichever form the caller used.
     *
     * @param functionCall  the call to read
     * @param position      the zero-based position of the parameter
     * @param parameterName the parameter's name
     * @return the argument expression if supplied, empty otherwise
     */
    public static Optional<ExpressionNode> getArgument(FunctionCallExpressionNode functionCall, int position,
                                                       String parameterName) {
        int[] positionalIndex = {0};
        for (FunctionArgumentNode argument : functionCall.arguments()) {
            switch (argument) {
                case NamedArgumentNode namedArgument
                        when parameterName.equals(namedArgument.argumentName().name().text()) ->
                        { return Optional.of(namedArgument.expression()); }
                case PositionalArgumentNode positionalArgument when positionalIndex[0]++ == position ->
                        { return Optional.of(positionalArgument.expression()); }
                default -> {
                    // A rest argument spreads a value that cannot be resolved without data-flow analysis
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find a field by name within a record. Computed and spread fields cannot be resolved statically and are
     * skipped.
     *
     * @param mappingConstructor the record to search
     * @param fieldName          the field name to look for
     * @return the matching field if present, empty otherwise
     */
    public static Optional<SpecificFieldNode> findField(MappingConstructorExpressionNode mappingConstructor,
                                                          String fieldName) {
        return mappingConstructor.fields().stream()
                .filter(field -> field.kind() == SyntaxKind.SPECIFIC_FIELD)
                .map(field -> (SpecificFieldNode) field)
                .filter(field -> matchesFieldName(field.fieldName(), fieldName))
                .findFirst();
    }

    private static boolean matchesFieldName(Node fieldNameNode, String expectedFieldName) {
        if (fieldNameNode instanceof IdentifierToken identifierToken) {
            return identifierToken.text().equals(expectedFieldName);
        }
        if (fieldNameNode instanceof BasicLiteralNode basicLiteralNode) {
            String literal = basicLiteralNode.literalToken().text();
            return literal.substring(1, literal.length() - 1).equals(expectedFieldName);
        }
        return false;
    }

    /**
     * Read a string value, following one level of variable assignment.
     * <p>
     * A command is commonly held in a variable rather than written at the call site, so a rule that only reads
     * literals would miss the ordinary way of writing the thing it is looking for.
     *
     * @param expression the expression to read
     * @return the string value if it can be resolved, empty otherwise
     */
    public static Optional<String> resolveStringValue(ExpressionNode expression) {
        Optional<String> literal = getStringLiteralValue(expression);
        if (literal.isPresent()) {
            return literal;
        }
        return resolveVariableInitializer(expression).flatMap(OsAnalysisUtils::getStringLiteralValue);
    }

    /**
     * Read the elements of a list, following one level of variable assignment.
     *
     * @param expression the expression to read
     * @return the list elements, or an empty list if the expression is not a resolvable list
     */
    public static List<ExpressionNode> resolveListElements(ExpressionNode expression) {
        ExpressionNode resolved = expression instanceof ListConstructorExpressionNode ? expression
                : resolveVariableInitializer(expression).orElse(expression);
        if (!(resolved instanceof ListConstructorExpressionNode listConstructor)) {
            return List.of();
        }
        return listConstructor.expressions().stream()
                .filter(ExpressionNode.class::isInstance)
                .map(ExpressionNode.class::cast)
                .toList();
    }

    /**
     * Resolve a simple name reference to the value the variable last held before this point.
     * <p>
     * Taking the declaration's initialiser alone would read a stale value: after
     * {@code string executable = "sh"; executable = "echo";} the call runs {@code echo}, and reporting on
     * {@code sh} would be reporting a command the program never executes. Only writes that appear before the
     * reference are considered, and the last of them wins.
     */
    private static Optional<ExpressionNode> resolveVariableInitializer(ExpressionNode expression) {
        if (expression.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String variableName = expression.toSourceCode().trim();
        int referenceOffset = expression.textRange().startOffset();
        for (Node scope = expression.parent(); scope != null; scope = scope.parent()) {
            Optional<ExpressionNode> resolved = resolveInScope(scope, variableName, referenceOffset);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return Optional.empty();
    }

    /**
     * Resolve the variable's last write within a single enclosing scope, without walking further up the tree.
     */
    private static Optional<ExpressionNode> resolveInScope(Node scope, String variableName, int referenceOffset) {
        if (scope instanceof BlockStatementNode block) {
            return findLastWrite(block.statements(), variableName, referenceOffset);
        }
        if (scope instanceof FunctionBodyBlockNode body) {
            return findLastWrite(body.statements(), variableName, referenceOffset);
        }
        if (scope instanceof ModulePartNode modulePart) {
            return findModuleLevelWrite(modulePart, variableName);
        }
        return Optional.empty();
    }

    private static Optional<ExpressionNode> findModuleLevelWrite(ModulePartNode modulePart, String variableName) {
        Optional<ExpressionNode> resolved = Optional.empty();
        for (Node member : modulePart.members()) {
            if (member instanceof ModuleVariableDeclarationNode declaration
                    && declaresVariable(declaration.typedBindingPattern().toSourceCode(), variableName)) {
                resolved = declaration.initializer();
            }
        }
        return resolved;
    }

    /**
     * Find the value the variable was last given by a declaration or an assignment appearing before the reference.
     */
    private static Optional<ExpressionNode> findLastWrite(NodeList<StatementNode> statements, String variableName,
                                                          int referenceOffset) {
        Optional<ExpressionNode> resolved = Optional.empty();
        for (StatementNode statement : statements) {
            if (statement.textRange().startOffset() >= referenceOffset) {
                break;
            }
            Optional<ExpressionNode> write = findWriteWithin(statement, variableName, referenceOffset);
            if (write.isPresent()) {
                resolved = write;
            }
        }
        return resolved;
    }

    /**
     * Find the last write to the variable within a statement, descending into any block it contains.
     * <p>
     * A write inside an {@code if} or a loop still assigns the variable, so a search limited to the top level of
     * the function would read a value the call never sees. A conditional write is taken as effective: for a
     * security rule, assuming the assignment happened is the safe direction.
     */
    private static Optional<ExpressionNode> findWriteWithin(Node node, String variableName, int referenceOffset) {
        if (node.textRange().startOffset() >= referenceOffset) {
            return Optional.empty();
        }
        if (node instanceof VariableDeclarationNode declaration
                && declaresVariable(declaration.typedBindingPattern().toSourceCode(), variableName)) {
            return declaration.initializer();
        }
        if (node instanceof AssignmentStatementNode assignment
                && variableName.equals(assignment.varRef().toSourceCode().trim())) {
            return Optional.of(assignment.expression());
        }
        if (!(node instanceof NonTerminalNode nonTerminal)) {
            return Optional.empty();
        }
        Optional<ExpressionNode> resolved = Optional.empty();
        for (Node child : nonTerminal.children()) {
            Optional<ExpressionNode> write = findWriteWithin(child, variableName, referenceOffset);
            if (write.isPresent()) {
                resolved = write;
            }
        }
        return resolved;
    }

    private static boolean declaresVariable(String typedBindingPattern, String variableName) {
        String[] tokens = typedBindingPattern.trim().split("\\s+");
        return tokens.length > 0 && variableName.equals(tokens[tokens.length - 1]);
    }

    /**
     * Get the value of a string literal expression, with the surrounding quotes removed.
     *
     * @param expression the expression to read
     * @return the literal value if the expression is a string literal, empty otherwise
     */
    public static Optional<String> getStringLiteralValue(ExpressionNode expression) {
        String source = expression.toSourceCode().trim();
        if (source.length() >= 2 && source.startsWith("\"") && source.endsWith("\"")) {
            return Optional.of(source.substring(1, source.length() - 1));
        }
        return Optional.empty();
    }

    /**
     * Check whether a value reaching this point came from a parameter of a public function.
     * <p>
     * A public function is the module's boundary, so its parameters are the closest thing to an untrusted input
     * this module can identify without a data-flow engine.
     *
     * @param node          the expression to check
     * @param semanticModel the semantic model
     * @return true if the value originates from a public function parameter
     */
    public static boolean isUserControlledInput(Node node, SemanticModel semanticModel) {
        Optional<Symbol> symbol = semanticModel.symbol(node);
        if (symbol.isEmpty()) {
            return false;
        }
        if (symbol.get().kind() == SymbolKind.PARAMETER && isInsidePublicFunction(node)) {
            return true;
        }
        return symbol.get().kind() == SymbolKind.VARIABLE && isAssignedUserControlledInput(node);
    }

    /**
     * Check whether any element of a list, or the list variable itself, carries user-controlled input.
     *
     * @param valueExpression the expression to check
     * @param semanticModel   the semantic model
     * @return true if user-controlled input reaches the expression
     */
    public static boolean containsUserControlledInput(ExpressionNode valueExpression, SemanticModel semanticModel) {
        if (valueExpression == null) {
            return false;
        }
        if (valueExpression instanceof ListConstructorExpressionNode listConstructor) {
            return listConstructor.expressions().stream()
                    .anyMatch(element -> isUserControlledInput(element, semanticModel));
        }
        return valueExpression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                && isUserControlledInput(valueExpression, semanticModel);
    }

    private static boolean isInsidePublicFunction(Node node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof FunctionDefinitionNode functionNode) {
                return functionNode.qualifierList().stream()
                        .anyMatch(qualifier -> qualifier.text().equals(PUBLIC_QUALIFIER));
            }
            parent = parent.parent();
        }
        return false;
    }

    /**
     * Check whether the value the variable last held before this point came from a public function parameter.
     * <p>
     * Resolution is bound to the reference: a write that appears after the call says nothing about the value the
     * call used, and a write inside a nested block is still a write. Both are handled by resolving the last
     * effective write through the enclosing scopes.
     */
    private static boolean isAssignedUserControlledInput(Node node) {
        if (!(node instanceof ExpressionNode reference)) {
            return false;
        }
        Optional<ExpressionNode> lastWrite = resolveVariableInitializer(reference);
        if (lastWrite.isEmpty()) {
            return false;
        }
        Node current = node.parent();
        while (current != null) {
            if (current instanceof FunctionDefinitionNode functionNode && isInsidePublicFunction(functionNode)) {
                for (Node parameter : functionNode.functionSignature().parameters()) {
                    if (parameter instanceof RequiredParameterNode requiredParameter
                            && requiredParameter.paramName().isPresent()
                            && isExpressionMatchingParam(lastWrite.get(),
                            requiredParameter.paramName().get().text())) {
                        return true;
                    }
                }
            }
            current = current.parent();
        }
        return false;
    }

    private static boolean isExpressionMatchingParam(ExpressionNode expression, String paramName) {
        if (expression.toSourceCode().equals(paramName)) {
            return true;
        }
        return expression instanceof ListConstructorExpressionNode listExpression
                && listExpression.expressions().stream().anyMatch(item -> item.toSourceCode().equals(paramName));
    }
}
