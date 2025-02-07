/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.api.tree.BlockFunctionBodyNode;
import io.ballerina.compiler.api.tree.ExpressionNode;
import io.ballerina.compiler.api.tree.ExpressionStatementNode;
import io.ballerina.compiler.api.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.api.tree.FunctionDefinitionNode;
import io.ballerina.compiler.api.tree.ParameterNode;
import io.ballerina.compiler.api.tree.StatementNode;
import io.ballerina.compiler.api.tree.ModulePartNode;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CodeAnalyzerTask;
import io.ballerina.projects.plugins.CodeAnalyzerTaskContext;
import io.ballerina.scan.Reporter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsRule.AVOID_UNSANITIZED_CMD_ARGS;

/**
 * The static code analyzer implementation for Ballerina Os package.
 */
public class OsStaticCodeAnalyzer extends CodeAnalyzer {
    private final Reporter reporter;

    public OsStaticCodeAnalyzer(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void init(CodeAnalysisContext analysisContext) {
        analysisContext.addCodeAnalyzerTask(new CodeAnalyzerTask<>() {
            @Override
            public void perform(CodeAnalyzerTaskContext taskContext) {
                SemanticModel semanticModel = taskContext.semanticModel();
                ModulePartNode modulePartNode = taskContext.currentPackage().modulePart();

                for (FunctionDefinitionNode functionNode : modulePartNode.members().stream()
                        .filter(FunctionDefinitionNode.class::isInstance)
                        .map(FunctionDefinitionNode.class::cast)
                        .collect(Collectors.toList())) {

                    List<String> paramNames = functionNode.functionSignature().parameters().stream()
                            .map(ParameterNode::paramName)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Token::text)
                            .collect(Collectors.toList());

                    if (functionNode.functionBody() instanceof BlockFunctionBodyNode) {
                        BlockFunctionBodyNode bodyNode = (BlockFunctionBodyNode) functionNode.functionBody();
                        boolean isSanitized = detectSanitization(bodyNode, paramNames);

                        for (StatementNode stmt : bodyNode.statements()) {
                            if (stmt instanceof ExpressionStatementNode) {
                                ExpressionNode expr = ((ExpressionStatementNode) stmt).expression();
                                if (isExecCommand(expr, semanticModel)) {
                                    if (!isSanitized && containsUserInput(expr, paramNames)) {
                                        taskContext.reportDiagnostic(reporter.report(
                                                stmt.location(),
                                                AVOID_UNSANITIZED_CMD_ARGS.getRule(),
                                                "Potential command injection in function '" +
                                                        functionNode.functionName().text() + "'. " +
                                                        "Unvalidated input is passed to os:exec(). " +
                                                        "Ensure proper sanitization using an allow-list or input filtering."
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isExecCommand(ExpressionNode expr, SemanticModel model) {
        if (expr instanceof FunctionCallExpressionNode callExpr) {
            Optional<Symbol> symbol = model.symbol(callExpr.functionName());
            return symbol.filter(s -> s.getName().orElse("").equals("exec")
                    && s.getModule().orElse("").toString().contains("ballerina/os")).isPresent();
        }
        return false;
    }

    private boolean containsUserInput(ExpressionNode expr, List<String> paramNames) {
        String exprText = expr.toSourceCode();
        return paramNames.stream().anyMatch(exprText::contains);
    }

    private boolean detectSanitization(BlockFunctionBodyNode bodyNode, List<String> paramNames) {
        for (StatementNode stmt : bodyNode.statements()) {
            String stmtText = stmt.toSourceCode();

            if (stmtText.contains(".some(") && stmtText.contains("equalsIgnoreCaseAscii")) {
                return true;
            }

            for (String param : paramNames) {
                if (stmtText.contains(param + ".replaceAll") ||
                        stmtText.contains(param + ".trim()") ||
                        stmtText.contains(param + ".toLowerAscii()")) {
                    return true;
                }
            }
        }
        return false;
    }
}
