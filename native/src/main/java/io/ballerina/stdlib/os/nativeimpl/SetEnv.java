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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.stdlib.os.utils.OSUtils.getEnvVariablesMap;

/**
 * Extern function of ballerina.os:setEnv.
 *
 * @since 1.2.2
 */
public class SetEnv {

    private SetEnv() {

    }

    public static Object setEnv(Environment env, BString key, Object value) {
        BMap<BString, Object> envMap = getEnvVariablesMap(env);
        if (value == null) {
            envMap.remove(key);
        } else {
            envMap.put(key, value);
        }
        return null;
    }
}
