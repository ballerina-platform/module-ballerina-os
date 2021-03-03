// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

# Returns the environment variable value associated with the provided name.
# ```ballerina
# string port = os:getEnv("HTTP_PORT");
# ```
#
# + name - Name of the environment variable
# + return - Environment variable value if it exists or else an empty string
public isolated function getEnv(@untainted string name) returns string {
    var value = java:toString(nativeGetEnv(java:fromString(name)));
    if (value is string) {
        return value;
    }
    return "";
}

isolated function nativeGetEnv(handle key) returns handle = @java:Method {
    name: "getenv",
    'class: "java.lang.System",
    paramTypes: ["java.lang.String"]
} external;

# Returns the current user's name.
# ```ballerina
# string username = os:getUsername();
# ```
#
# + return - Current user's name if it can be determined or else an empty string
public isolated function getUsername() returns string = @java:Method {
    name: "getUsername",
    'class: "org.ballerinalang.stdlib.os.nativeimpl.GetUsername"
} external;

# Returns the current user's home directory path.
# ```ballerina
# string userHome = os:getUserHome();
# ```
#
# + return - Current user's home directory if it can be determined or else an empty string
public isolated function getUserHome() returns string = @java:Method {
    name: "getUserHome",
    'class: "org.ballerinalang.stdlib.os.nativeimpl.GetUserHome"
} external;
