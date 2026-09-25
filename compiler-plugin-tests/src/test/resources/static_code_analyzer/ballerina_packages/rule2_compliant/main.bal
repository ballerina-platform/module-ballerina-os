// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org)
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/os;

// The compliant example from the rule documentation - the input is validated against an
// alphanumeric pattern before it is written to the environment
public function setValidatedEnv(string configPath) returns string|error {
    if configPath.matches(re `^[a-zA-Z0-9]*$`) {
        os:Error? err = os:setEnv("CONFIG_PATH", configPath);
        if err is os:Error {
            return error("Failed to set environment variable");
        }
        return "Environment variable set successfully";
    }
    return error("Invalid input: Only alphanumeric characters are allowed");
}

// The value is a constant written in the code
public function setConstantEnv() returns os:Error? {
    return os:setEnv("APP_MODE", "production");
}

// The value comes from a named argument that is a constant
public function setConstantEnvNamed() returns os:Error? {
    return os:setEnv(key = "APP_MODE", value = "production");
}

// The parameter only chooses the key, not the value written to it
public function setEnvWithConstantValue(string userInput) returns os:Error? {
    string mode = "production";
    string logged = userInput;
    return os:setEnv("APP_MODE", mode);
}

// The value is overwritten with a constant before the call
public function setEnvReassigned(string userInput) returns os:Error? {
    string mode = userInput;
    mode = "production";
    return os:setEnv("APP_MODE", mode);
}

// Unsetting a variable writes no user-controlled value
public function unsetEnv() returns os:Error? {
    return os:unsetEnv("APP_MODE");
}

// The early-return form of the same pattern check
public function setEnvAfterGuard(string configPath) returns os:Error? {
    if !configPath.matches(re `^[a-zA-Z0-9]*$`) {
        return error os:Error("Invalid input: Only alphanumeric characters are allowed");
    }
    return os:setEnv("CONFIG_PATH", configPath);
}

// The parameter is written to the variable only after the call, so the call uses the constant
public function assignedAfterUse(string userInput) returns os:Error? {
    string mode = "production";
    check os:setEnv("APP_MODE", mode);
    mode = userInput;
    return;
}

// The compliant examples from the rule documentation
public function configure(string userInput) returns os:Error? {
    if !["production", "staging"].some(mode => mode == userInput) {
        return error("unknown mode");
    }
    check os:setEnv("APP_MODE", userInput);
}

public function setConfigPath(string configPath) returns string|error {
    // Compliant: input restricted to alphanumeric characters before use
    if (re `^[a-zA-Z0-9]*$`).isFullMatch(configPath) {
        os:Error? err = os:setEnv("CONFIG_PATH", configPath);

        if err is os:Error {
            return error("Failed to set environment variable");
        }
        return "Environment variable set successfully";
    } else {
        return error("Invalid input: Only alphanumeric characters are allowed");
    }
}
