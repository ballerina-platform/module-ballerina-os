/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.stdlib.os.utils;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.Map;

import static io.ballerina.stdlib.os.nativeimpl.ModuleUtils.getModule;
import static io.ballerina.stdlib.os.utils.OSConstants.ENV_VAR_KEY;
import static io.ballerina.stdlib.os.utils.OSConstants.PROCESS_FIELD;
import static io.ballerina.stdlib.os.utils.OSConstants.PROCESS_TYPE;

/**
 * @since 0.8.0
 */
public class OSUtils {

    public static BObject getProcessObject(Process process) throws IOException {
        BObject obj = ValueCreator.createObjectValue(getModule(), PROCESS_TYPE);
        obj.addNativeData(PROCESS_FIELD, process);
        return obj;
    }

    public static Process processFromObject(BObject objVal) {
        return (Process) objVal.getNativeData(PROCESS_FIELD);
    }

    /**
     * Returns the os property which corresponds to the given key.
     *
     * @param key os property key
     * @return os property as a {@link String} or {@code PredefinedTypes.TYPE_STRING.getZeroValue()} if the
     * property does not exist.
     */
    public static BString getSystemProperty(BString key) {
        String value = System.getProperty(key.toString());
        if (value == null) {
            return StringUtils.fromString(
                    io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING.getZeroValue().toString());
        }
        return StringUtils.fromString(value);
    }

    public static BMap<BString, Object> getEnvVariablesMap(Environment env) {
        Object envVarMap = env.getStrandLocal(ENV_VAR_KEY);
        BMap<BString, Object> envMap;
        if (envVarMap != null) {
            return (BMap<BString, Object>) envVarMap;
        }
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_STRING);
        envMap = ValueCreator.createMapValue(mapType);
        Map<String, String> jEnvMap = System.getenv();
        for (Map.Entry<String, String> entry : jEnvMap.entrySet()) {
            envMap.put(StringUtils.fromString(entry.getKey()), StringUtils.fromString(entry.getValue()));
        }
        env.setStrandLocal(ENV_VAR_KEY, envMap);
        return envMap;
    }
}
