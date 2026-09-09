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

import io.ballerina.scan.Rule;

import static io.ballerina.scan.RuleKind.VULNERABILITY;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.RuleFactory.createRule;

/**
 * Represents static code rules specific to the Ballerina OS package.
 */
public enum OSRule {
    AVOID_UNSANITIZED_CMD_ARGS(createRule(1, "Avoid constructing system command arguments from user input " +
            "without proper sanitization", VULNERABILITY)),
    AVOID_UNSANITIZED_ENV_VARS(createRule(2, "Avoid constructing environment variables from user input " +
            "without proper sanitization", VULNERABILITY)),
    AVOID_SHELL_INVOCATION(createRule(3, "Avoid executing commands through a shell interpreter", VULNERABILITY)),
    AVOID_UNQUALIFIED_EXECUTABLE_PATH(createRule(4, "Avoid executing commands resolved through the PATH " +
            "environment variable", VULNERABILITY));

    private final Rule rule;

    OSRule(Rule rule) {
        this.rule = rule;
    }

    public int getId() {
        return this.rule.numericId();
    }

    public String getDescription() {
        return this.rule.description();
    }

    @Override
    public String toString() {
        return "{\"id\":" + this.getId() + ", \"kind\":\"" + this.rule.kind() + "\"," +
                " \"description\" : \"" + this.rule.description() + "\"}";
    }
}
