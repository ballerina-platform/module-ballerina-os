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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.stdlib.os.utils.OSUtils;

import java.io.IOException;
import java.io.InputStream;

import static io.ballerina.stdlib.os.nativeimpl.Errors.ProcessExecError;

/**
 * External function for ballerina.os:Process.output.
 *
 * @since 1.4.0
 */
public class Output {

    private Output() {

    }

    private static final ArrayType BYTE_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_BYTE);

    public static Object output(BObject objVal, long fileOutputStream) {
        BArray byteDataArray = ValueCreator.createArrayValue(BYTE_ARRAY_TYPE);
        byte[] result;
        Process process = OSUtils.processFromObject(objVal);
        InputStream in;
        if (fileOutputStream == 1) {
            in = process.getInputStream();
        } else {
            in = process.getErrorStream();
        }
        try {
            result = in.readAllBytes();
        } catch (IOException e) {
            return ErrorCreator.createError(ModuleUtils.getModule(), String.valueOf(ProcessExecError),
                    StringUtils.fromString("Failed to read the output of the process" + ": " + e.getMessage()),
                    null, null);
        }
        for (int i = 0; i < result.length; i++) {
            byteDataArray.add(i, result[i]);
        }
        return byteDataArray;
    }
}
