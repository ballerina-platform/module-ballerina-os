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

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.containsUserControlledInput;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.ARGUMENTS_FIELD;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.EXEC;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_UNSANITIZED_CMD_ARGS;

/**
 * Rule to detect user-controlled input reaching the arguments of an executed command.
 * <p>
 * An argument the caller controls lets an attacker change what the command does, and where the command is a shell
 * it lets them run a command of their own choosing.
 */
public class AvoidUnsanitizedCommandArgumentsRule implements OsFunctionRule {

    @Override
    public void analyze(OsFunctionContext context) {
        context.getCommandFieldValue(ARGUMENTS_FIELD)
                .filter(arguments -> containsUserControlledInput(arguments, context.getSemanticModel()))
                .ifPresent(arguments -> context.reportIssue(context.getFunctionLocation(), getRuleId()));
    }

    @Override
    public int getRuleId() {
        return AVOID_UNSANITIZED_CMD_ARGS.getId();
    }

    @Override
    public boolean isApplicable(OsFunctionContext context) {
        return EXEC.equals(context.getFunctionName()) && context.hasCommandRecord();
    }
}
