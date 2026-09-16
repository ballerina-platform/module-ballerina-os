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

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.scan.Reporter;

import java.util.Optional;
import java.util.Set;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.collectOsPrefixes;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.getDocument;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.getOsFunctionName;

/**
 * Analyzes calls into the {@code ballerina/os} module.
 */
public class OsFunctionCallAnalyzer implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final Reporter reporter;
    private final OsFunctionRulesEngine rulesEngine;

    public OsFunctionCallAnalyzer(Reporter reporter) {
        this.reporter = reporter;
        this.rulesEngine = new OsFunctionRulesEngine();
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (!(context.node() instanceof FunctionCallExpressionNode functionCall)) {
            return;
        }
        Set<String> osPrefixes = collectOsPrefixes(context);
        Optional<String> functionName = getOsFunctionName(functionCall, osPrefixes, context.semanticModel());
        if (functionName.isEmpty()) {
            return;
        }
        rulesEngine.executeRules(new OsFunctionContext(reporter, getDocument(context), context.semanticModel(),
                functionName.get(), functionCall));
    }
}
