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

package io.ballerina.stdlib.os.compiler.staticcodeanalyzer.osrules;

import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsFunctionContext;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.isUserControlledInput;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.SET_ENV;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_UNSANITIZED_ENV_VARS;

/**
 * Rule to detect user-controlled input reaching an environment variable value.
 * <p>
 * Environment variables are inherited by every process the service starts, so a value the caller controls changes
 * the behaviour of programs well outside the code that set it.
 */
public class AvoidUnsanitizedEnvironmentVariablesRule implements OsFunctionRule {

    @Override
    public void analyze(OsFunctionContext context) {
        context.getEnvironmentValue()
                .filter(value -> isUserControlledInput(value, context.getSemanticModel()))
                .ifPresent(value -> context.reportIssue(context.getFunctionLocation(), getRuleId()));
    }

    @Override
    public int getRuleId() {
        return AVOID_UNSANITIZED_ENV_VARS.getId();
    }

    @Override
    public boolean isApplicable(OsFunctionContext context) {
        return SET_ENV.equals(context.getFunctionName());
    }
}
