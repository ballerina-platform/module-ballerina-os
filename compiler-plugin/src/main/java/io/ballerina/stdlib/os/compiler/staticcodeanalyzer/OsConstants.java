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

/**
 * Names from the {@code ballerina/os} API that the analysis rules match against.
 */
public final class OsConstants {

    public static final String OS = "os";
    public static final String BALLERINA_ORG = "ballerina";

    public static final String EXEC = "exec";
    public static final String SET_ENV = "setEnv";

    public static final String COMMAND_PARAM = "command";
    public static final String VALUE_PARAM = "value";
    public static final int COMMAND_POSITION = 0;
    public static final int VALUE_POSITION = 1;

    public static final String VALUE_FIELD = "value";
    public static final String ARGUMENTS_FIELD = "arguments";

    private OsConstants() {
    }
}
