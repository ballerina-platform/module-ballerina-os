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

import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.osrules.AvoidShellInvocationRule;
import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.osrules.AvoidUnqualifiedExecutablePathRule;
import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.osrules.AvoidUnsanitizedCommandArgumentsRule;
import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.osrules.AvoidUnsanitizedEnvironmentVariablesRule;
import io.ballerina.stdlib.os.compiler.staticcodeanalyzer.osrules.OsFunctionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine to execute OS function rules.
 */
public class OsFunctionRulesEngine {

    private final List<OsFunctionRule> rules;

    public OsFunctionRulesEngine() {
        this.rules = new ArrayList<>();
        initializeDefaultRules();
    }

    public void executeRules(OsFunctionContext context) {
        for (OsFunctionRule rule : rules) {
            if (rule.isApplicable(context)) {
                rule.analyze(context);
            }
        }
    }

    public void addRule(OsFunctionRule rule) {
        if (rule != null && !rules.contains(rule)) {
            rules.add(rule);
        }
    }

    private void initializeDefaultRules() {
        addRule(new AvoidUnsanitizedCommandArgumentsRule());
        addRule(new AvoidUnsanitizedEnvironmentVariablesRule());
        addRule(new AvoidShellInvocationRule());
        addRule(new AvoidUnqualifiedExecutablePathRule());
        // Add more default rules here as needed
    }
}
