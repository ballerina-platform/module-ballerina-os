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
import io.ballerina.stdlib.os.utils.OSUtils;

/**
 * External function for ballerina.os:Process.exit.
 *
 * @since 1.4.0
 */
public class Exit {

    private Exit() {

    }

    public static void exit(BObject objVal) {
        Process process = OSUtils.processFromObject(objVal);
        process.destroy();
    }
}
