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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.scan.Reporter;
import io.ballerina.tools.diagnostics.Location;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_UNSANITIZED_CMD_ARGS;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_UNSANITIZED_ENV_VARS;

/**
 * Analyzes function calls for potential command injection vulnerabilities.
 */
public class OSCommandInjectionAnalyzer implements AnalysisTask<SyntaxNodeAnalysisContext> {
    private final Reporter reporter;
    private static final String PUBLIC_QUALIFIER = "public";
    private static final String OS = "os";
    private static final String EXEC = "exec";
    private static final String ARGUMENTS = "arguments";
    private static final String BALLERINA_ORG = "ballerina";
    private static final String SET_ENV = "setEnv";
    private static final String VALUE = "value";

    public OSCommandInjectionAnalyzer(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (!(context.node() instanceof FunctionCallExpressionNode functionCall)) {
            return;
        }

        Document document = getDocument(context);
        List<String> importPrefix = new ArrayList<>();
        if (document.syntaxTree().rootNode() instanceof ModulePartNode modulePartNode) {
            importPrefix = modulePartNode.imports().stream()
                    .filter(importDeclarationNode -> {
                        ImportOrgNameNode importOrgNameNode = importDeclarationNode.orgName().orElse(null);
                        return importOrgNameNode != null && BALLERINA_ORG.equals(importOrgNameNode.orgName().text());
                    })
                    .filter(importDeclarationNode -> importDeclarationNode.moduleName().stream()
                            .anyMatch(moduleNameNode -> OS.equals(moduleNameNode.text())))
                    .map(importDeclarationNode -> {
                        ImportPrefixNode importPrefixNode = importDeclarationNode.prefix().orElse(null);
                        return importPrefixNode != null ? importPrefixNode.prefix().text() : OS;
                    }).toList();
        }

        if (isOsExecCall(functionCall, importPrefix)
                && containsUserControlledInput(functionCall.arguments(), context)) {
            Location location = functionCall.location();
            this.reporter.reportIssue(document, location, AVOID_UNSANITIZED_CMD_ARGS.getId());
        }

        if (isOsSetEnvCall(functionCall, importPrefix)
                && containsUntrustedEnvValue(functionCall.arguments(), context)) {
            Location location = functionCall.location();
            this.reporter.reportIssue(document, location, AVOID_UNSANITIZED_ENV_VARS.getId());
        }
    }

    public static boolean isOsExecCall(FunctionCallExpressionNode functionCall, List<String> importPrefix) {
        if (!(functionCall.functionName() instanceof QualifiedNameReferenceNode qNode)) {
            return false;
        }

        return importPrefix.contains(qNode.modulePrefix().text()) && qNode.identifier().text().equals(EXEC);
    }

    public static boolean isOsSetEnvCall(FunctionCallExpressionNode functionCall, List<String> importPrefix) {
        if (!(functionCall.functionName() instanceof QualifiedNameReferenceNode qNode)) {
            return false;
        }

        return importPrefix.contains(qNode.modulePrefix().text()) && qNode.identifier().text().equals(SET_ENV);
    }

    public static Document getDocument(SyntaxNodeAnalysisContext context) {
        return context.currentPackage().module(context.moduleId()).document(context.documentId());
    }

    private boolean containsUserControlledInput(SeparatedNodeList<FunctionArgumentNode> arguments,
                                                SyntaxNodeAnalysisContext context) {
        for (FunctionArgumentNode arg : arguments) {
            ExpressionNode expr = extractExpression(arg);
            if (expr == null) {
                continue;
            }

            if (isMappingConstructorWithUserControlledInput(expr, context)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsUntrustedEnvValue(SeparatedNodeList<FunctionArgumentNode> arguments,
                                              SyntaxNodeAnalysisContext context) {
        int idx = 0;
        for (FunctionArgumentNode arg : arguments) {
            ExpressionNode expr = extractExpression(arg);
            if (expr == null) {
                idx++;
                continue;
            }

            if (((arg instanceof NamedArgumentNode named && named.argumentName().name().text().equals(VALUE))
                    || (arg instanceof PositionalArgumentNode && idx == 1)) && isUserControlledInput(expr, context)) {
                return true;
            }
            idx++;
        }

        return false;
    }

    private ExpressionNode extractExpression(FunctionArgumentNode arg) {
        return switch (arg) {
            case PositionalArgumentNode posArg -> posArg.expression();
            case NamedArgumentNode namedArg -> namedArg.expression();
            default -> null;
        };
    }

    private boolean isMappingConstructorWithUserControlledInput(ExpressionNode expr,
                                                                SyntaxNodeAnalysisContext context) {
        if (!(expr instanceof MappingConstructorExpressionNode mappingNode)) {
            return false;
        }

        return mappingNode.fields().stream()
                .filter(SpecificFieldNode.class::isInstance)
                .map(SpecificFieldNode.class::cast)
                .anyMatch(specificField -> isUserControlledField(specificField, context));
    }

    private boolean isUserControlledField(SpecificFieldNode specificField,
                                          SyntaxNodeAnalysisContext context) {
        String fieldName = specificField.fieldName().toString().trim();
        if (!fieldName.equals(ARGUMENTS)) {
            return false;
        }

        ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
        return containsUserControlledInput(valueExpr, context);
    }

    private boolean containsUserControlledInput(ExpressionNode valueExpr,
                                                SyntaxNodeAnalysisContext context) {
        if (valueExpr instanceof ListConstructorExpressionNode listNode) {
            return listNode.expressions().stream()
                    .anyMatch(item -> isUserControlledInput(item, context));
        }

        return valueExpr instanceof SimpleNameReferenceNode refNode
                && isUserControlledInput(refNode, context);
    }

    private boolean isUserControlledInput(Node node, SyntaxNodeAnalysisContext context) {
        SemanticModel semanticModel = context.semanticModel();
        if (semanticModel.symbol(node).isEmpty()) {
            return false;
        }

        Symbol symbol = semanticModel.symbol(node).get();

        if (symbol.kind() == SymbolKind.PARAMETER && isInsidePublicFunction(node)) {
            return true;
        }

        if (symbol.kind() == SymbolKind.VARIABLE) {
            return isAssignedUserControlledInput(node);
        }

        return false;
    }

    private boolean isInsidePublicFunction(Node node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof FunctionDefinitionNode functionNode) {
                return functionNode.qualifierList().stream()
                        .anyMatch(q -> q.text().equals(PUBLIC_QUALIFIER));
            }
            parent = parent.parent();
        }

        return false;
    }

    private boolean isAssignedUserControlledInput(Node node) {
        Node parent = node.parent();

        while (parent != null) {
            if (parent instanceof FunctionDefinitionNode functionNode && isInsidePublicFunction(functionNode)) {
                for (var param : functionNode.functionSignature().parameters()) {
                    if (param instanceof RequiredParameterNode requiredParam &&
                            requiredParam.paramName().isPresent()) {
                        String paramName = requiredParam.paramName().get().text();

                        if (isVariableAssignedFrom(node, paramName, functionNode)) {
                            return true;
                        }
                    }
                }
            }
            parent = parent.parent();
        }

        return false;
    }

    private boolean isVariableAssignedFrom(Node variable, String paramName,
                                           FunctionDefinitionNode functionNode) {
        FunctionBodyNode body = functionNode.functionBody();

        if (body instanceof FunctionBodyBlockNode blockBody) {
            for (var statement : blockBody.statements()) {
                if (isVariableDeclaredWithParam(statement, paramName)) {
                    return true;
                }

                if (isVariableAssignedWithParam(statement, variable, paramName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isVariableDeclaredWithParam(StatementNode statement, String paramName) {
        if (!(statement instanceof VariableDeclarationNode varDecl)) {
            return false;
        }

        return varDecl.initializer()
                .map(initializer -> isExpressionMatchingParam(initializer, paramName))
                .orElse(false);
    }

    private boolean isVariableAssignedWithParam(StatementNode statement, Node variable, String paramName) {
        if (!(statement instanceof AssignmentStatementNode assignment)) {
            return false;
        }

        String assignedVar = assignment.varRef().toSourceCode();
        ExpressionNode assignedValue = assignment.expression();

        if (!assignedVar.equals(variable.toSourceCode())) {
            return false;
        }

        return isExpressionMatchingParam(assignedValue, paramName);
    }

    private boolean isExpressionMatchingParam(ExpressionNode expression, String paramName) {
        if (expression.toSourceCode().equals(paramName)) {
            return true;
        }

        if (expression instanceof ListConstructorExpressionNode listExpr) {
            return listExpr.expressions().stream()
                    .anyMatch(item -> item.toSourceCode().equals(paramName));
        }

        return false;
    }
}
