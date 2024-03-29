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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.os.utils.OSUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.ballerina.stdlib.os.nativeimpl.Errors.ProcessExecError;

/**
 * Extern function os:exec.
 *
 * @since 1.4.0
 */
public class Exec {

    static final String VALUE = "value";
    static final String ARGUMENTS = "arguments";

    private Exec() {

    }
    
    public static Object exec(BMap<BString, Object> command, BMap<BString, Object> env) {
        List<String> commandList = new ArrayList<>();
        commandList.add(command.getStringValue(StringUtils.fromString(VALUE)).getValue());
        String[] arguments = command.getArrayValue(StringUtils.fromString(ARGUMENTS)).getStringArray();

        Collections.addAll(commandList, arguments);
        ProcessBuilder pb = new ProcessBuilder(commandList);
        if (env != null) {
            Map<String, String> pbEnv = pb.environment();
            env.entrySet().forEach(entry -> pbEnv.put(entry.getKey().getValue(), entry.getValue().toString()));
        }
        try {
            return OSUtils.getProcessObject(pb.start());
        } catch (IOException e) {
            return ErrorCreator.createError(ModuleUtils.getModule(), String.valueOf(ProcessExecError),
                    StringUtils.fromString("Failed to retrieve the process object" + ": " + e.getMessage()),
                    null, null);
        }
    }
}
