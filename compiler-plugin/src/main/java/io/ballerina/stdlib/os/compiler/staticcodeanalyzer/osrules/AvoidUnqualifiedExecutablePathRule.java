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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsFunctionContext;

import java.util.Optional;
import java.util.regex.Pattern;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.resolveStringValue;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.EXEC;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.VALUE_FIELD;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_UNQUALIFIED_EXECUTABLE_PATH;

/**
 * Rule to detect a command named without a path.
 * <p>
 * A bare name is resolved through {@code PATH} at run time, so which program actually runs depends on the
 * environment the service happens to start in rather than on the code. Anyone able to place a file earlier in
 * {@code PATH} — or to set {@code PATH} itself, which {@code os:setEnv} allows — chooses the program that executes.
 */
public class AvoidUnqualifiedExecutablePathRule implements OsFunctionRule {

    /**
     * Windows resolves {@code C:tool.exe} against that drive's current directory rather than through
     * {@code PATH}, so it is a relative path and not what this rule reports.
     */
    private static final Pattern DRIVE_RELATIVE = Pattern.compile("^[A-Za-z]:");

    @Override
    public void analyze(OsFunctionContext context) {
        Optional<ExpressionNode> value = context.getCommandFieldValue(VALUE_FIELD);
        if (value.isEmpty()) {
            return;
        }
        boolean isUnqualified = resolveStringValue(value.get())
                .filter(executable -> !executable.isEmpty())
                .filter(executable -> !DRIVE_RELATIVE.matcher(executable).find())
                .map(executable -> !executable.contains("/") && !executable.contains("\\"))
                .orElse(false);
        if (isUnqualified) {
            context.reportIssue(value.get().location(), getRuleId());
        }
    }

    @Override
    public int getRuleId() {
        return AVOID_UNQUALIFIED_EXECUTABLE_PATH.getId();
    }

    @Override
    public boolean isApplicable(OsFunctionContext context) {
        return EXEC.equals(context.getFunctionName()) && context.hasCommandRecord();
    }
}
