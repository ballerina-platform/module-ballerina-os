/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.stdlib.os.utils.OSUtils.getEnvVariablesMap;

/**
 * Extern function of ballerina.os:getEnv.
 *
 * @since 1.5.1
 */
public class GetEnv {

    private GetEnv() {

    }

    public static BString getEnv(Environment env, BString key) {
        BMap<BString, Object> envMap = getEnvVariablesMap(env);
        Object value = envMap.get(key);
        if (value == null) {
            return StringUtils.fromString("");
        }
        return (BString) value;
    }
}
