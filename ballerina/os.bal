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

public type Command record {|
    string value;
    string[] arguments = [];
|};

public type EnvProperties record {|
    never command?;
    anydata...;
|};

# Returns the environment variable value associated with the provided name.
# ```ballerina
# string port = os:getEnv("HTTP_PORT");
# ```
#
# + name - Name of the environment variable
# + return - Environment variable value if it exists or else, an empty string
public isolated function getEnv(string name) returns string {
    var value = java:toString(nativeGetEnv(java:fromString(name)));
    if value is string {
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
# + return - Current user's name if it can be determined or else, an empty string
public isolated function getUsername() returns string = @java:Method {
    name: "getUsername",
    'class: "io.ballerina.stdlib.os.nativeimpl.GetUsername"
} external;

# Returns the current user's home directory path.
# ```ballerina
# string userHome = os:getUserHome();
# ```
#
# + return - Current user's home directory if it can be determined or else, an empty string
public isolated function getUserHome() returns string = @java:Method {
    name: "getUserHome",
    'class: "io.ballerina.stdlib.os.nativeimpl.GetUserHome"
} external;

# Sets the value of the environment variable named by the key. 
# Note that the parameter key cannot be an empty string or "==" sign.
# ```ballerina
# os:Error? err = os:setEnv("BALCONFIGFILE", "/path/to/Config.toml");
# ```
#
# + key - Key of the environment variable
# + value - Value of the environment variable
# + return - An `os:Error` if setting the environment variable fails, () otherwise
public isolated function setEnv(string key, string value) returns Error? {
    if key == "" {
        return error Error("The parameter key cannot be an empty string");
    } else if key == "==" {
        return error Error("The parameter key cannot be == sign");
    } else {
        return setEnvExtern(key, value);
    }
}

# Removes a single environment variable from the system if it exists.
# Note that the parameter key cannot be an empty string.
# ```ballerina
# os:Error? err = os:unsetEnv("BALCONFIGFILE");
# ```
#
# + key - Key of the environment variable
# + return - An `os:Error` if unsetting the environment variable fails, () otherwise
public isolated function unsetEnv(string key) returns Error? {
    if key == "" {
        return error Error("The parameter key cannot be an empty string");
    } else {
        return setEnvExtern(key, ());
    }
}

isolated function setEnvExtern(string key, string? value) returns Error? = @java:Method {
    name: "setEnv",
    'class: "io.ballerina.stdlib.os.nativeimpl.SetEnv"
} external;

# Returns a map of environment variables.
# ```ballerina
# map<string> envs = os:listEnv();
# ```
#
# + return - Map of environment variables
public isolated function listEnv() returns map<string> {
    return listEnvExtern();
}

isolated function listEnvExtern() returns map<string> = @java:Method {
    name: "listEnv",
    'class: "io.ballerina.stdlib.os.nativeimpl.ListEnv"
} external;

# Executes an operating system command as a subprocess of the current process.
# ```ballerina
# os:Process|os:Error result = os:exec({value: "bal", arguments: ["run", filepath]}, BAL_CONFIG_FILE = "/abc/Config.toml");
# ```
#
# + command - The command to be executed
# + envProperties - The environment properties
#
# + return - Process object in success, or an Error if a failure occurs
public isolated function exec(Command command, *EnvProperties envProperties) returns Process|Error = @java:Method {
    name: "exec",
    'class: "io.ballerina.stdlib.os.nativeimpl.Exec"
} external;
