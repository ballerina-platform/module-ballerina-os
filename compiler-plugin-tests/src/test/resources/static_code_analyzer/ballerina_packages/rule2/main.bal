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

import ballerina/io;
import ballerina/os;

public function setEnv(string userInput) returns os:Error? {
    os:Error? result = check os:setEnv(
            key = "BALCONFIGFILE",
            value = userInput
    );
    return result;
}

public function main() {
    string envValue = "/path/to/Config.toml";
    os:Error? output = setEnv(envValue);

    if output is os:Error {
        io:println("Error setting environment variable: ", output);
    }
}

// Negative case - an unrelated declaration is fed by the parameter, but the value
// passed to setEnv is not, so taint must not carry across to it
public function setSafeEnv(string userInput) returns os:Error? {
    string logged = userInput;
    string safeValue = "production";
    check os:setEnv("APP_MODE", safeValue);
    return;
}

// Negative case - the write happens after the call, so it says nothing about
// the value the call actually used
public function assignedAfterUse(string userInput) returns os:Error? {
    string mode = "production";
    check os:setEnv("APP_MODE", mode);
    mode = userInput;
    return;
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
