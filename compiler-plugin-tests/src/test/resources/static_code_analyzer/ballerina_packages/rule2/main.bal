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

public function setEnv(string userInput) returns os:Error? {
    os:Error? result = check os:setEnv(
            key = "BALCONFIGFILE",
            value = userInput
    );
    return result;
}

// A write inside a nested block still reaches the call that follows it
public function assignedInNestedBlock(string userInput, boolean override) returns os:Error? {
    string mode = "production";
    if override {
        mode = userInput;
    }
    check os:setEnv("APP_MODE", mode);
    return;
}

// Measuring the input checks nothing about its content
public function setEnvLengthChecked(string userInput) returns os:Error? {
    if userInput.length() < 64 {
        return os:setEnv("APP_MODE", userInput);
    }
    return;
}

// The check validates a different value from the one written to the environment
public function setEnvOtherValueChecked(string userInput, string mode) returns os:Error? {
    if mode.matches(re `^[a-z]+$`) {
        return os:setEnv("APP_MODE", userInput);
    }
    return;
}

// The non-compliant examples from the rule documentation
public function configure(string userInput) returns os:Error? {
    check os:setEnv("APP_MODE", userInput);
}

public function setConfigPath(string configPath) returns error? {
    // Noncompliant: user-supplied input flows into os:setEnv
    os:Error? err = os:setEnv("CONFIG_PATH", configPath);

    if err is os:Error {
        return error("Failed to set environment variable");
    }
}
