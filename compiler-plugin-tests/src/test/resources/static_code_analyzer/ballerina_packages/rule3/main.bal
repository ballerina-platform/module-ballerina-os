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

// A command string handed to a shell, where every metacharacter is interpreted again
public function shellCommand() returns os:Process|error {
    return check os:exec({
        value: "/bin/sh",
        arguments: ["-c", "ls -la /tmp"]
    });
}

// The same through variables, which is the ordinary way of writing it
public function shellCommandViaVariables() returns os:Process|error {
    string shell = "/bin/bash";
    string[] shellArguments = ["-c", "echo hello"];
    return check os:exec({
        value: shell,
        arguments: shellArguments
    });
}

// The Windows command interpreter
public function windowsShellCommand() returns os:Process|error {
    return check os:exec({
        value: "cmd.exe",
        arguments: ["/c", "dir"]
    });
}

// Negative case - the executable is run directly with separated arguments
public function directCommand() returns os:Process|error {
    return check os:exec({
        value: "/bin/ls",
        arguments: ["-la", "/tmp"]
    });
}

// Negative case - a shell running a script by path, with no command string to re-parse
public function shellScript() returns os:Process|error {
    return check os:exec({
        value: "/bin/sh",
        arguments: ["/opt/scripts/backup.sh"]
    });
}

// Negative case - the shell is overwritten before the call, so the command that
// actually runs is the one assigned last
public function reassignedBeforeUse() returns os:Process|error {
    string executable = "/bin/sh";
    executable = "/bin/echo";
    return check os:exec({
        value: executable,
        arguments: ["-c", "hello"]
    });
}
