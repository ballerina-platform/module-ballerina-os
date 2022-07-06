/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.stdlib.os.nativeimpl;

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.stdlib.os.utils.OSConstants;
import io.ballerina.stdlib.os.utils.OSUtils;

/**
 * External function for ballerina.os:Process.waitForExit.
 *
 * @since 1.3.1
 */
public class WaitForExit {

    public static Object waitForExit(BObject objVal) {
        Process process = OSUtils.processFromObject(objVal);
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            return OSUtils.getBallerinaError(OSConstants.PROCESS_EXEC_ERROR, e);
        }
    }
}
