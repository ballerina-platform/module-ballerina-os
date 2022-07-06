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
import ballerina/io;

configurable string bal_exec_path = ?;

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
        ["!@#$%^&*()_+~", "test3"],
        ["key with spaces", "test 4"]
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
        ["!@#$%^&*()_+~", "test3"],
        ["key with spaces", "test 4"]
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

@test:Config {}
function testUnsetEnvNegative() {
    Error? result = unsetEnv("");
    if result is Error {
        test:assertEquals(result.message(), "The parameter key cannot be an empty string");
    } else {
        test:assertFail("setEnv did not return an error for empty string as key");
    }
}

function testListEnv() {
    map<string> env = listEnv();
    test:assertTrue(env.length() > 0);
}

@test:Config {}
function testGetSystemPropertyNegative() {
    test:assertEquals(getSystemProperty("non-existing-key"), "");
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

@test:Config {}
function testExec() returns error? {
    Process process = check exec({value: "echo", arguments: ["hello world"]});
    int exitCode = check process.waitForExit();
    test:assertEquals(exitCode, 0);

    byte[] outputBytes = check process.output();
    string outputString = check string:fromBytes(outputBytes);
    test:assertEquals(outputString.trim(), "hello world");
}

@test:Config {}
function testExecWithOutputStdOut() returns error? {
    Process process = check exec({value: bal_exec_path, arguments: ["run", "tests/resources/hello1.bal"]});
    int exitCode = check process.waitForExit();
    test:assertEquals(exitCode, 0);

    byte[] stdOutBytes = check process.output(io:stdout);
    string stdOutString = check string:fromBytes(stdOutBytes);
    test:assertTrue(stdOutString.includes("hello world"));

    byte[] stdErrBytes = check process.output(io:stderr);
    string stdErrString = check string:fromBytes(stdErrBytes);
    test:assertFalse(stdErrString.includes("hello world"));
}

@test:Config {}
function testExecWithOutputStdErr() returns error? {
    Process process = check exec({value: bal_exec_path, arguments: ["run", "tests/resources/hello2.bal"]});
    int exitCode = check process.waitForExit();
    test:assertEquals(exitCode, 0);

    byte[] stdOutBytes = check process.output(io:stdout);
    string stdOutString = check string:fromBytes(stdOutBytes);
    test:assertFalse(stdOutString.includes("hello world"));

    byte[] stdErrBytes = check process.output(io:stderr);
    string stdErrString = check string:fromBytes(stdErrBytes);
    test:assertTrue(stdErrString.includes("hello world"));
}

@test:Config {}
function testExecWithEnvironmentVariable() returns error? {
    Process process = check exec({value: bal_exec_path, arguments: ["run", "tests/resources/hello3.bal"]}, BAL_CONFIG_FILES = "tests/resources/config/Config.toml");
    int exitCode = check process.waitForExit();
    test:assertEquals(exitCode, 0);

    byte[] outputBytes = check process.output(io:stderr);
    string outputString = check string:fromBytes(outputBytes);
    test:assertTrue(outputString.includes("{\"time\":\""));
    test:assertTrue(outputString.includes("\", \"level\":\"DEBUG\", \"module\":\"\", \"message\":\"debug message\"}"));
}

@test:Config {}
function testExecExit() returns error? {
    Process process = check exec({value: "echo", arguments: ["hello world"]});
    process.exit();
}    
