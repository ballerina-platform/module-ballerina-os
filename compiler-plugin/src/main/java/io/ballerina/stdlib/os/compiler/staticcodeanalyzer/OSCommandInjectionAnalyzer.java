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

import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.projects.Document;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.scan.Reporter;
import io.ballerina.stdlib.os.compiler.OSCompilerPluginUtil;
import io.ballerina.tools.diagnostics.Location;

import static io.ballerina.stdlib.os.compiler.OSCompilerPluginUtil.isOsExecCall;
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

        if (!isOsExecCall(context, functionCall)) {
            return;
        }

        Document document = OSCompilerPluginUtil.getDocument(context);

        if (containsUserControlledInput(functionCall.arguments(), context)) {
            Location location = functionCall.location();
            this.reporter.reportIssue(document, location, AVOID_UNSANITIZED_CMD_ARGS.getId());
        }
    }

    private boolean containsUserControlledInput(SeparatedNodeList<FunctionArgumentNode> arguments,
                                                SyntaxNodeAnalysisContext context) {
        for (Node arg : arguments) {
            if (isUserControlledInput(arg, context)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserControlledInput(Node node, SyntaxNodeAnalysisContext context) {
        // Use the semantic model to check if the node is derived from user input
        return context.semanticModel().symbol(node).isPresent()
                && context.semanticModel().symbol(node).get().kind() == SymbolKind.PARAMETER;
    }
}
