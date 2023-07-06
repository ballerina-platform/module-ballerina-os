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

import io.ballerina.runtime.api.values.BString;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * Extern function of ballerina.os:setEnv.
 *
 * @since 1.2.2
 */
public class SetEnv {

    private static final String JAVA_LANG_PROCESS_ENVIRONMENT = "java.lang.ProcessEnvironment";
    private static final String CASE_INSENSITIVE_ENV = "theCaseInsensitiveEnvironment";

    private SetEnv() {

    }

    @SuppressWarnings("unchecked")
    public static Object setEnv(BString key, Object value) {
        Map<String, String> env = null;
        Map<String, String> writableEnv;
        Field field;
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                field = Class.forName(JAVA_LANG_PROCESS_ENVIRONMENT).getDeclaredField(CASE_INSENSITIVE_ENV);
            } catch (NoSuchFieldException | ClassNotFoundException e) {
                return ErrorGenerator.createError("Error while accessing the environment variable map" , e);
            }
        } else {
            env = System.getenv();
            try {
                field = env.getClass().getDeclaredField("m");
            } catch (NoSuchFieldException e) {
                return ErrorGenerator.createError("Error while accessing the environment variable map", e);
            }
        }
        Field finalField = field;
        AccessController.doPrivileged((PrivilegedAction) () -> {
            finalField.setAccessible(true);
            return null;
        });
        try {
            writableEnv = (Map<String, String>) field.get(env);
        } catch (IllegalAccessException e) {
            return ErrorGenerator.createError("Access denied when trying to modify the environment variable map", e);
        }
        if (value == null) {
            writableEnv.remove(key.toString());
        } else {
            writableEnv.put(key.toString(), value.toString());
        }
        return null;
    }
}
