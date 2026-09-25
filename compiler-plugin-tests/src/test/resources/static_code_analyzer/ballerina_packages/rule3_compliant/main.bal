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

// The executable is run directly with its arguments as separate list elements
public function directCommand() returns os:Process|error {
    return check os:exec({
        value: "/bin/ls",
        arguments: ["-la", "/tmp"]
    });
}

// The same through variables
public function directCommandViaVariables() returns os:Process|error {
    string executable = "/usr/bin/git";
    string[] arguments = ["log", "--oneline"];
    return check os:exec({
        value: executable,
        arguments: arguments
    });
}

// A shell running a script by path, with no command string to re-parse
public function shellScript() returns os:Process|error {
    return check os:exec({
        value: "/bin/bash",
        arguments: ["/opt/scripts/backup.sh"]
    });
}

// PowerShell running a script file rather than a command string
public function powershellScript() returns os:Process|error {
    return check os:exec({
        value: "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe",
        arguments: ["-File", "C:\\scripts\\backup.ps1"]
    });
}

// A `-c` flag passed to a program that is not a shell has no shell meaning
public function nonShellWithCommandFlag() returns os:Process|error {
    return check os:exec({
        value: "/usr/bin/git",
        arguments: ["-c", "core.pager=cat", "log"]
    });
}

// A shell with no arguments at all
public function shellWithoutArguments() returns os:Process|error {
    return check os:exec({
        value: "/bin/sh"
    });
}

// The shell is overwritten before the call, so the program that runs is not a shell
public function reassignedBeforeUse() returns os:Process|error {
    string executable = "/bin/sh";
    executable = "/bin/echo";
    return check os:exec({
        value: executable,
        arguments: ["-c", "hello"]
    });
}

// The compliant example from the rule documentation
public function countFiles() returns os:Process|error {
    return check os:exec({value: "/bin/ls", arguments: ["/var/data"]});
}
