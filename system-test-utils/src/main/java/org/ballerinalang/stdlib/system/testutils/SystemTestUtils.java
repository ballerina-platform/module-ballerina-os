/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.system.testutils;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.api.BString;

public class SystemTestUtils {

    public static BString testValidEnv() {
        return StringUtils.fromString(System.getenv("JAVA_HOME"));
    }

    public static BString testGetUserHome() {
        return StringUtils.fromString(System.getProperty("user.home"));
    }

    public static BString testGetUserName() {
        return StringUtils.fromString(System.getProperty("user.name"));
    }

    public static boolean testOs() {
        return org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
    }
}
