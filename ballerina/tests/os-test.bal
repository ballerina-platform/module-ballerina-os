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

import ballerina/io;
import ballerina/jballerina.java;
import ballerina/test;

@test:Config {}
function testValidEnv() {
    string expectedValue = getExpectedValidEnv();
    test:assertEquals(getEnv("JAVA_HOME"), expectedValue);
}

@test:Config {}
isolated function testEmptyEnv() {
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

@test:Config {}
function testGetSystemPropertyNegative() {
    test:assertEquals(getSystemPropertyExtern("non-existing-key"), "");
}

function toString(io:ReadableByteChannel input) returns string|error {
    string result = "";
    io:ReadableCharacterChannel charIn = new(input, "UTF-8");
    while (true) {
        var x = charIn.read(1);
        if (x is error) { break; }
        else { result = result + x; }
    }
    check charIn.close();
    return <@untainted> result;
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

isolated function isWindowsEnvironment() returns boolean = @java:Method {
    name: "isWindowsEnvironment",
    'class: "io.ballerina.stdlib.os.testutils.EnvironmentTestUtils"
} external;

function getSystemPropertyExtern(string key) returns string = @java:Method {
    name: "getSystemPropertyExtern",
    'class: "io.ballerina.stdlib.os.utils.OSUtils"
} external;
