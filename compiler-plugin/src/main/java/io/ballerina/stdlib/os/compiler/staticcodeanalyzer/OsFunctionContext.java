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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.projects.Document;
import io.ballerina.scan.Reporter;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.findField;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsAnalysisUtils.getArgument;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.COMMAND_PARAM;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.COMMAND_POSITION;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.EXEC;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.VALUE_PARAM;
import static io.ballerina.stdlib.os.compiler.staticcodeanalyzer.OsConstants.VALUE_POSITION;

/**
 * Represents the context of an OS module function call being analyzed.
 * <p>
 * The {@code Command} record passed to {@code os:exec} is resolved once here, so a rule states the property it cares
 * about rather than repeating how to reach the record.
 */
public class OsFunctionContext {

    private final Reporter reporter;
    private final Document document;
    private final SemanticModel semanticModel;
    private final String functionName;
    private final Location functionLocation;
    private final MappingConstructorExpressionNode commandRecord;
    private final ExpressionNode environmentValue;

    /**
     * Creates a context for the given OS module function call.
     *
     * @param reporter      the static code analysis reporter
     * @param document      the document containing the call
     * @param semanticModel the semantic model
     * @param functionName  the simple name of the OS function being called
     * @param functionCall  the call being analyzed
     */
    public OsFunctionContext(Reporter reporter, Document document, SemanticModel semanticModel, String functionName,
                             FunctionCallExpressionNode functionCall) {
        this.reporter = reporter;
        this.document = document;
        this.semanticModel = semanticModel;
        this.functionName = functionName;
        this.functionLocation = functionCall.location();
        this.commandRecord = resolveCommandRecord(functionCall, functionName);
        this.environmentValue = getArgument(functionCall, VALUE_POSITION, VALUE_PARAM).orElse(null);
    }

    private static MappingConstructorExpressionNode resolveCommandRecord(FunctionCallExpressionNode functionCall,
                                                                        String functionName) {
        if (!EXEC.equals(functionName)) {
            return null;
        }
        return getArgument(functionCall, COMMAND_POSITION, COMMAND_PARAM)
                .filter(MappingConstructorExpressionNode.class::isInstance)
                .map(MappingConstructorExpressionNode.class::cast)
                .orElse(null);
    }

    public SemanticModel getSemanticModel() {
        return this.semanticModel;
    }

    /**
     * The simple name of the OS function that was called, such as {@code exec}.
     *
     * @return the called function's name
     */
    public String getFunctionName() {
        return this.functionName;
    }

    /**
     * The location of the whole call.
     *
     * @return the location of the function call
     */
    public Location getFunctionLocation() {
        return this.functionLocation;
    }

    /**
     * Whether the {@code Command} record could be resolved at the call site.
     *
     * @return true if a command record is available
     */
    public boolean hasCommandRecord() {
        return this.commandRecord != null;
    }

    /**
     * Get a field of the {@code Command} record.
     *
     * @param fieldName the field name to look for
     * @return the field if the record was resolved and carries it, empty otherwise
     */
    public Optional<SpecificFieldNode> getCommandField(String fieldName) {
        return this.commandRecord == null ? Optional.empty() : findField(this.commandRecord, fieldName);
    }

    /**
     * Get the value of a field of the {@code Command} record.
     *
     * @param fieldName the field name to look for
     * @return the field's value if the record was resolved and carries it, empty otherwise
     */
    public Optional<ExpressionNode> getCommandFieldValue(String fieldName) {
        return getCommandField(fieldName).flatMap(SpecificFieldNode::valueExpr);
    }

    /**
     * The value argument of {@code os:setEnv}.
     *
     * @return the value argument if supplied, empty otherwise
     */
    public Optional<ExpressionNode> getEnvironmentValue() {
        return Optional.ofNullable(this.environmentValue);
    }

    /**
     * Report an issue against this call.
     *
     * @param location the location to report at
     * @param ruleId   the rule reporting the issue
     */
    public void reportIssue(Location location, int ruleId) {
        this.reporter.reportIssue(this.document, location, ruleId);
    }
}
