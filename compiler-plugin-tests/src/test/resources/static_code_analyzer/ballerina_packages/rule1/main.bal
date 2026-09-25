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

public function executeCommand(string userInput) returns os:Process|error {
    string terminalPath = "/bin/sh";
    string[] cmd = ["-c", userInput];

    os:Process result = check os:exec({
        value: terminalPath,
        arguments: cmd
    });

    return result;
}

final string[] & readonly ALLOWED_ARGUMENTS = ["main", "main.bal", "bal"];

// A comparison checks nothing about the content, so the input is still unchecked
public function executeNonEmptyChecked(string userInput) returns os:Process|error? {
    if userInput != "" {
        return check os:exec({
            value: "/usr/bin/bal",
            arguments: ["run", userInput]
        });
    }
    return;
}

// The call is in the branch taken when the allow-list check fails
public function executeInFailedBranch(string userInput) returns os:Process|error? {
    if ALLOWED_ARGUMENTS.indexOf(userInput) != () {
        return;
    } else {
        return check os:exec({
            value: "/usr/bin/bal",
            arguments: ["run", userInput]
        });
    }
}

// The check validates a different value from the one passed to the command
public function executeOtherValueChecked(string userInput, string mode) returns os:Process|error? {
    if ALLOWED_ARGUMENTS.indexOf(mode) != () {
        return check os:exec({
            value: "/usr/bin/bal",
            arguments: ["run", userInput]
        });
    }
    return;
}

// The non-compliant examples from the rule documentation
public function listDirectory(string userInput) returns os:Process|error {
    return check os:exec({value: "/bin/ls", arguments: [userInput]});
}

public function execCommand(string input) returns error? {
    string terminalPath = "/usr/bin/bal";
    string[] cmd = ["run", input];

    // Noncompliant: user-supplied input flows into an os:exec argument
    os:Process result = check os:exec({
        value: terminalPath,
        arguments: cmd
    });
    _ = check result.waitForExit();
}
