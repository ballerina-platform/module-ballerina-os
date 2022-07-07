// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# This object contains information on a process being created from Ballerina.
# This is returned from the `exec` function in the `os` module.
public class Process {

    # Waits for the process to finish its work and exit. 
    # This will return 0 if successful, or a different value during failure depending on the operating system.
    # ```ballerina
    # int|os:Error exitCode = process.waitForExit();
    # ```
    #
    # + return - Returns the exit code for the process, or else an `Error` if a failure occurs
    public isolated function waitForExit() returns int|Error {
        return nativeWaitForExit(self);
    }

    # Returns the standard output as default. Option provided to return standard error by providing file descriptor.
    # If the process was not finished and exited explicitly by running process.waitForExit(), then process.output() will finish the work and exit and return the output. 
    # ```ballerina
    # byte[]|os:Error err = process.output(io:stderr);
    # ```
    #
    # + fileOutputStream - The output stream (`io:stdout` or `io:stderr`) content needs to be returned
    # 
    # + return - The `byte[]`, which represents the process's 'standard error', or the 'standard out', or an Error
    public isolated function output(io:FileOutputStream fileOutputStream = io:stdout) returns byte[]|Error {
        return nativeOutput(self, fileOutputStream);
    }

    # Terminates the process.
    # ```ballerina
    # process.exit();
    # ```
    #
    public isolated function exit() {
        return nativeExit(self);
    }
}

isolated function nativeWaitForExit(Process process) returns int|Error = @java:Method {
    name: "waitForExit",
    'class: "io.ballerina.stdlib.os.nativeimpl.WaitForExit"
} external;

isolated function nativeExit(Process process) = @java:Method {
    name: "exit",
    'class: "io.ballerina.stdlib.os.nativeimpl.Exit"
} external;

isolated function nativeOutput(Process process, int fileOutputStream) returns byte[]|Error = @java:Method {
    name: "output",
    'class: "io.ballerina.stdlib.os.nativeimpl.Output"
} external;
