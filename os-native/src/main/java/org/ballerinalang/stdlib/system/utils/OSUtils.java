/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.os.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;

import static org.ballerinalang.stdlib.os.nativeimpl.ModuleUtils.getModule;

/**
 * @since 0.94.1
 */
public class OSUtils {

    /**
     * Returns the os property which corresponds to the given key.
     *
     * @param key os property key
     * @return os property as a {@link String} or {@code PredefinedTypes.TYPE_STRING.getZeroValue()} if the
     * property does not exist.
     */
    public static String getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING.getZeroValue();
        }
        return value;
    }

    private OSUtils() {
    }
}
