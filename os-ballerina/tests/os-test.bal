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
import ballerina/java;
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
function testExecInUnixLike1() returns error? {
    Process x1 = check exec("env", { "BAL_EXEC_TEST_VAR":"X" });
    Process x2 = check exec("grep", {}, (), "BAL_EXEC_TEST_VAR");
    var x2out = x1.pipe(x2).stdout();
    int ec1 = check x2.waitForExit();
    int ec2 = check x2.exitCode();
    string result = check toString(x2out);
    test:assertEquals(result.trim(), "BAL_EXEC_TEST_VAR=X");
    test:assertEquals(ec1, 0);
    test:assertEquals(ec2, 0);
}

@test:Config {}
function testExecInUnixLike2() returns error? {
    Process x1 = check exec("pwd", {}, "/");
    var x1out = x1.stdout();
    string result = check toString(x1out);
    test:assertEquals(result.trim(), "/");
}

@test:Config {}
function testExecInUnixLike3() returns error? {
    Process x1 = check exec("grep", {}, (), "BAL_TEST");
    io:WritableDataChannel ch = new(x1.stdin());
    check ch.writeString("BAL_TEST", "UTF-8");
    check ch.close();
    string result = check toString(x1.stdout());
    test:assertEquals(result.trim(), "BAL_TEST");
}

@test:Config {}
function testExecInUnixLike4() returns error? {
    Process x1 = check exec("env", { "BAL_EXEC_TEST_VAR":"X" });
    Process x2 = check exec("grep", {}, (), "BAL_EXEC_TEST_VAR");
    Process x3 = check exec("wc", {}, (), "-l");
    var x3out = x1.pipe(x2).pipe(x3).stdout();
    string result = check toString(x3out);
    test:assertEquals(result.trim(), "1");
}

@test:Config {}
isolated function testExecWithError() {
    string expected = "Cannot run program \"eee\": error=2, No such file or directory";
    Process|error x1 = exec("eee", {}, (), "BAL_EXEC_TEST_VAR");
    if (x1 is error) {
       test:assertEquals(x1.message(), expected);
    } else {
        test:assertFail("Didn't receive the expected error message: " + expected);
    }
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
    'class: "org.ballerinalang.stdlib.os.testutils.OSTestUtils"
} external;

function getExpectedUserHome() returns string = @java:Method {
    name: "testGetUserHome",
    'class: "org.ballerinalang.stdlib.os.testutils.OSTestUtils"
} external;

function getExpectedUserName() returns string = @java:Method {
    name: "testGetUserName",
    'class: "org.ballerinalang.stdlib.os.testutils.OSTestUtils"
} external;
