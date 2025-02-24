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
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.scan.Reporter;
import io.ballerina.tools.diagnostics.Location;

import static io.ballerina.stdlib.os.compiler.Constants.EXEC;
import static io.ballerina.stdlib.os.compiler.Constants.OS;
import static io.ballerina.stdlib.os.compiler.Constants.PUBLIC_QUALIFIER;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_UNSANITIZED_CMD_ARGS;

public class OSCommandInjectionAnalyzer implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final Reporter reporter;

    public OSCommandInjectionAnalyzer(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (!(context.node() instanceof FunctionCallExpressionNode functionCall)) {
            return;
        }

        if (!isOsExecCall(functionCall)) {
            return;
        }

        Document document = getDocument(context);

        if (containsUserControlledInput(functionCall.arguments(), context)) {
            Location location = functionCall.location();
            this.reporter.reportIssue(document, location, AVOID_UNSANITIZED_CMD_ARGS.getId());
        }
    }

    public static boolean isOsExecCall(FunctionCallExpressionNode functionCall) {
        if (!(functionCall.functionName() instanceof QualifiedNameReferenceNode qNode)) {
            return false;
        }
        return qNode.modulePrefix().text().equals(OS) && qNode.identifier().text().equals(EXEC);
    }

    public static Document getDocument(SyntaxNodeAnalysisContext context) {
        return context.currentPackage().module(context.moduleId()).document(context.documentId());
    }

    private boolean containsUserControlledInput(SeparatedNodeList<FunctionArgumentNode> arguments,
                                                SyntaxNodeAnalysisContext context) {
        for (FunctionArgumentNode arg : arguments) {
            // Extract the expression inside the argument node
            ExpressionNode expr;
            if (arg instanceof PositionalArgumentNode posArg) {
                expr = posArg.expression();
            } else if (arg instanceof NamedArgumentNode namedArg) {
                expr = namedArg.expression();
            } else {
                continue;
            }

            // Check if the extracted expression is a record (mapping constructor)
            if (expr instanceof MappingConstructorExpressionNode mappingNode) {
                for (MappingFieldNode field : mappingNode.fields()) {
                    if (field instanceof SpecificFieldNode specificField) {
                        String fieldName = specificField.fieldName().toString().trim();

                        // Extract and check the `arguments` field
                        if (fieldName.equals("arguments")) {
                            ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
                            if (valueExpr instanceof ListConstructorExpressionNode listNode) {
                                for (Node listItem : listNode.expressions()) {
                                    if (isUserControlledInput(listItem, context)) {
                                        return true;
                                    }
                                }
                            } else if (valueExpr instanceof SimpleNameReferenceNode refNode) {
                                // Check if the variable is assigned user-controlled input
                                if (isUserControlledInput(refNode, context)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isUserControlledInput(Node node, SyntaxNodeAnalysisContext context) {
        SemanticModel semanticModel = context.semanticModel();
        if (semanticModel == null) {
            return false;
        }

        if (semanticModel.symbol(node).isEmpty()) {
            return false;
        }

        Symbol symbol = semanticModel.symbol(node).get();

        if (symbol.kind() == SymbolKind.PARAMETER && isInsidePublicFunction(node)) {
            return true;
        }

        if (symbol.kind() == SymbolKind.VARIABLE) {
            return isAssignedUserControlledInput(node, context);
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
            parent = node.parent();
        }
        return false;
    }

    private boolean isAssignedUserControlledInput(Node node, SyntaxNodeAnalysisContext context) {
        Node parent = node.parent();

        // Traverse up the AST to find where the variable is assigned
        while (parent != null) {
            if (parent instanceof FunctionDefinitionNode functionNode && isInsidePublicFunction(functionNode)) {
                // Check if this variable is assigned from a function parameter
                for (var param : functionNode.functionSignature().parameters()) {
                    String paramName = ((RequiredParameterNode) param).paramName().get().text();

                    // Check if this variable (node) is assigned from a function parameter
                    if (isVariableAssignedFrom(node, paramName, functionNode, context)) {
                        return true;
                    }
                }
            }
            parent = parent.parent();
        }
        return false;
    }

    private boolean isVariableAssignedFrom(Node variable, String paramName,
                                           FunctionDefinitionNode functionNode, SyntaxNodeAnalysisContext context) {
        FunctionBodyNode body = functionNode.functionBody();

        if (body instanceof FunctionBodyBlockNode blockBody) {
            for (var statement : blockBody.statements()) {
                if (statement instanceof VariableDeclarationNode varDecl) {
                    if (varDecl.initializer().isPresent()) {
                        ExpressionNode initializer = varDecl.initializer().get();
                        if (initializer instanceof ListConstructorExpressionNode listExpr) {
                            for (var listItem : listExpr.expressions()) {
                                if (listItem.toSourceCode().equals(paramName)) {
                                    return true;
                                }
                            }
                        } else if (initializer.toSourceCode().equals(paramName)) {
                            return true;
                        }
                    }
                } else if (statement instanceof AssignmentStatementNode assignment) {
                    String assignedVar = assignment.varRef().toSourceCode();
                    ExpressionNode assignedValue = assignment.expression();

                    if (assignedVar.equals(variable.toSourceCode())) {
                        if (assignedValue.toSourceCode().equals(paramName)) {
                            return true;
                        }

                        if (assignedValue instanceof ListConstructorExpressionNode listExpr) {
                            for (var listItem : listExpr.expressions()) {
                                if (listItem.toSourceCode().equals(paramName)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
