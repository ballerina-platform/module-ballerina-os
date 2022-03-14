// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/test;

@test:Config {}
function testGetEnv() {
    string expectedValue = getExpectedValidEnv();
    test:assertEquals(getEnv("JAVA_HOME"), expectedValue);
}

@test:Config {}
isolated function testGetEnvNegative() {
    test:assertEquals(getEnv("JAVA_XXXX"), "");
}

@test:Config {}
function testGetUserHome() {
    test:assertEquals(getUserHome(), getExpectedUserHome());
}

@test:Config {}
function testGetUsername() {
    test:assertEquals(getUsername(), getExpectedUserName());
}

function setEnvDataProvider() returns (string[][]) {
    return [
        ["foo", "test1"],
        ["0x00", "test2"],
        ["!@#$%^&*()_+~", "test3"]
    ];
}

@test:Config {
    dataProvider: setEnvDataProvider
}
function testSetEnv(string key, string value) {
    Error? result = setEnv(key, value);
    if result is Error {
        test:assertFail("failed to set environment variable with key " + key + " : " + result.message());
    } else {
        test:assertEquals(getEnv(key), value);
    }
}

function setEnvDataProviderNegative() returns (string[][]) {
    return [
        ["", "test1", "The parameter key cannot be an empty string"],
        ["==", "test2", "The parameter key cannot be == sign"]
    ];
}

@test:Config {
    dataProvider: setEnvDataProviderNegative
}
function testSetEnvNegative(string key, string value, string errorMessage) {
    Error? result = setEnv(key, value);
    if result is Error {
        test:assertEquals(result.message(), errorMessage);
    } else {
        test:assertFail("setEnv did not return an error for " + key + " as key");
    }
}

function unsetEnvDataProvider() returns (string[][]) {
    return [
        ["foo", "test1"],
        ["0x00", "test2"],
        ["!@#$%^&*()_+~", "test3"]
    ];
}

@test:Config {
    dataProvider: unsetEnvDataProvider
}
function testUnsetEnv(string key, string value) {
    Error? result = setEnv(key, value);
    if result is Error {
        test:assertFail("failed to set environment variable with key " + key + " : " + result.message());
    } else {
        test:assertEquals(getEnv(key), value);
        result = unsetEnv(key);
        if result is Error {
            test:assertFail("failed to unset environment variable with key " + key + " : " + result.message());
        } else {
            test:assertFalse(envVariableExists(key), "environment variable with key " + key + " has not been removed");
        }
    }
}

function envVariableExists(string key) returns boolean {
    map<string> env = listEnv();
    foreach [string, string] [k, v] in env.entries() {
        if k == key && v != "" {
            return true;
        }
    }
    return false;
}

@test:Config {}
function testUnsetEnvNegative() {
    Error? result = unsetEnv("");
    if result is Error {
        test:assertEquals(result.message(), "The parameter key cannot be an empty string");
    } else {
        test:assertFail("setEnv did not return an error for empty string as key");
    }
}

function listEnvDataProvider() returns (string[][]) {
    return [
        ["foo1", "bar1"],
        ["foo2", "bar2"],
        ["foo3", "bar3"]
    ];
}

@test:Config {
    dataProvider: listEnvDataProvider
}
function testListEnv(string key, string value) {
    boolean envExists = false;
    Error? result = setEnv(key, value);
    if result is Error {
        test:assertFail("failed to set environment variable with key " + key + " : " + result.message());
    } else {
        map<string> env = listEnv();
        foreach [string, string] [k, v] in env.entries() {
            if k == key && v == value {
                envExists = true;
            }
        }
        test:assertTrue(envExists);
    }
}

@test:Config {}
function testGetSystemPropertyNegative() {
    test:assertEquals(getSystemProperty("non-existing-key"), "");
}

function getExpectedValidEnv() returns string = @java:Method {
    name: "testValidEnv",
    'class: "io.ballerina.stdlib.os.testutils.OSTestUtils"
} external;

function getExpectedUserHome() returns string = @java:Method {
    name: "testGetUserHome",
    'class: "io.ballerina.stdlib.os.testutils.OSTestUtils"
} external;

function getExpectedUserName() returns string = @java:Method {
    name: "testGetUserName",
    'class: "io.ballerina.stdlib.os.testutils.OSTestUtils"
} external;

function getSystemProperty(string key) returns string = @java:Method {
    name: "getSystemProperty",
    'class: "io.ballerina.stdlib.os.utils.OSUtils"
} external;
