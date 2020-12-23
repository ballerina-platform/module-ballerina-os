/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.system.utils;

/**
 * Constants for system package functions.
 *
 * @since 0.995.0
 */
public class SystemConstants {
    /**
     * Organization name.
     */
    public static final String ORG_NAME = "ballerina";

    /**
     * Package name.
     */
    public static final String PACKAGE_NAME = "system";

    static final String PROCESS_TYPE = "Process";

    static final String PROCESS_FIELD = "ProcessField";

    // System error type names
    public static final String PROCESS_EXEC_ERROR = "ProcessExecError";

    // System constant fields
    public static final int DEFAULT_MAX_DEPTH = -1;

    private SystemConstants() {
    }
}
