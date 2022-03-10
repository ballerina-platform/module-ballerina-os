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

    @SuppressWarnings("unchecked")
    public static Object setEnv(BString key, BString value) {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Class<?> sc = Class.forName("java.lang.ProcessEnvironment");
                Field caseInsensitiveField = sc.getDeclaredField("theCaseInsensitiveEnvironment");
                AccessController.doPrivileged((PrivilegedAction) () -> {
                    caseInsensitiveField.setAccessible(true);
                    return null;
                });
                Map<String, String> writableEnv = (Map<String, String>) caseInsensitiveField.get(null);
                writableEnv.put(key.toString(), value.toString());
            } else {
                Map<String, String> env = System.getenv();
                Class<?> cl = env.getClass();
                Field field = cl.getDeclaredField("m");
                AccessController.doPrivileged((PrivilegedAction) () -> {
                    field.setAccessible(true);
                    return null;
                });
                Map<String, String> writableEnv = (Map<String, String>) field.get(env);
                writableEnv.put(key.toString(), value.toString());
            }
        } catch (RuntimeException e) {
            return ErrorGenerator.createError("runtime exception occurred: " + e.getMessage());
        } catch (Exception e) {
            return ErrorGenerator.createError("exception occurred: " + e.getMessage());
        }
        return null;
    }
}
