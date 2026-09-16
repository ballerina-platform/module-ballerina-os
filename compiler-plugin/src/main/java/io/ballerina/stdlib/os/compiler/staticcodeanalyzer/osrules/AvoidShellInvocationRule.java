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

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.resolveListElements;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.resolveStringValue;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.ARGUMENTS_FIELD;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.EXEC;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.VALUE_FIELD;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OSRule.AVOID_SHELL_INVOCATION;

/**
 * Rule to detect a command executed through a shell interpreter.
 * <p>
 * {@code os:exec} takes the executable and its arguments separately, which is what keeps an argument from being read
 * as syntax. Running {@code sh -c} throws that away: the argument becomes a script, and every metacharacter in it —
 * {@code ;}, {@code |}, {@code $()} — is interpreted again. A value that was safe as an argument becomes a command
 * of its own.
 */
public class AvoidShellInvocationRule implements OsFunctionRule {

    private static final Set<String> SHELL_EXECUTABLES = Set.of("sh", "bash", "zsh", "ksh", "dash", "csh", "tcsh",
            "fish", "cmd", "cmd.exe", "powershell", "powershell.exe", "pwsh", "pwsh.exe");

    private static final Set<String> COMMAND_STRING_FLAGS = Set.of("-c", "/c", "/k", "-command", "-Command",
            "-EncodedCommand", "-encodedcommand");

    @Override
    public void analyze(OsFunctionContext context) {
        Optional<ExpressionNode> value = context.getCommandFieldValue(VALUE_FIELD);
        if (value.isEmpty() || !isShellExecutable(value.get())) {
            return;
        }
        if (hasCommandStringFlag(context)) {
            context.reportIssue(context.getFunctionLocation(), getRuleId());
        }
    }

    /**
     * Match the executable on its file name, so an absolute path such as {@code /bin/sh} is recognised as readily as
     * a bare {@code sh}.
     */
    private boolean isShellExecutable(ExpressionNode value) {
        return resolveStringValue(value)
                .map(executable -> executable.substring(executable.lastIndexOf('/') + 1))
                .map(executable -> executable.substring(executable.lastIndexOf('\\') + 1))
                .map(executable -> SHELL_EXECUTABLES.contains(executable.toLowerCase(Locale.ROOT)))
                .orElse(false);
    }

    /**
     * A shell is only a command interpreter here when it is handed a command string. Running a shell script by path
     * carries none of the same re-parsing.
     */
    private boolean hasCommandStringFlag(OsFunctionContext context) {
        Optional<ExpressionNode> arguments = context.getCommandFieldValue(ARGUMENTS_FIELD);
        if (arguments.isEmpty()) {
            return false;
        }
        List<ExpressionNode> elements = resolveListElements(arguments.get());
        return elements.stream()
                .map(element -> resolveStringValue(element).orElse(""))
                .anyMatch(argument -> COMMAND_STRING_FLAGS.contains(argument.toLowerCase(Locale.ROOT)));
    }

    @Override
    public int getRuleId() {
        return AVOID_SHELL_INVOCATION.getId();
    }

    @Override
    public boolean isApplicable(OsFunctionContext context) {
        return EXEC.equals(context.getFunctionName()) && context.hasCommandRecord();
    }
}
