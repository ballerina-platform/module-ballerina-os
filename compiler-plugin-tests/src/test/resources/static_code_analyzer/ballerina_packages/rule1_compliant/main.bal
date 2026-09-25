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

final string[] & readonly ALLOWED_ARGUMENTS = ["main", "main.bal", "bal"];

// The compliant example from the rule documentation - the input is checked against an
// allow-list before the command that carries it is executed
public function executeAllowListed(string userInput) returns os:Process|error? {
    string[] cmd = ["run", userInput];
    if ALLOWED_ARGUMENTS.some(keyword => keyword.equalsIgnoreCaseAscii(userInput)) {
        return check os:exec({
            value: "/usr/bin/bal",
            arguments: cmd
        });
    }
    return;
}

// Every argument is a constant written in the code
public function executeConstantArguments() returns os:Process|error {
    return check os:exec({
        value: "/usr/bin/git",
        arguments: ["status", "--short"]
    });
}

// The parameter feeds an unrelated variable, never the arguments
public function executeUnrelatedParameter(string userInput) returns os:Process|error {
    string logged = userInput;
    string[] cmd = ["status"];
    return check os:exec({
        value: "/usr/bin/git",
        arguments: cmd
    });
}

// The arguments are overwritten with constants before the call
public function executeReassignedArguments(string userInput) returns os:Process|error {
    string[] cmd = [userInput];
    cmd = ["status"];
    return check os:exec({
        value: "/usr/bin/git",
        arguments: cmd
    });
}

// The parameter is only assigned after the call has already run
public function executeAssignedAfterUse(string userInput) returns os:Process|error {
    string[] cmd = ["status"];
    os:Process process = check os:exec({
        value: "/usr/bin/git",
        arguments: cmd
    });
    cmd = [userInput];
    return process;
}

// The early-return form of the same allow-list check
public function executeAfterGuard(string userInput) returns os:Process|error {
    if !ALLOWED_ARGUMENTS.some(keyword => keyword.equalsIgnoreCaseAscii(userInput)) {
        return error("Argument is not allowed");
    }
    return check os:exec({
        value: "/usr/bin/bal",
        arguments: ["run", userInput]
    });
}

// The input is validated by a helper function before the call
public function executeHelperValidated(string userInput) returns os:Process|error? {
    if isAllowedArgument(userInput) {
        return check os:exec({
            value: "/usr/bin/bal",
            arguments: ["run", userInput]
        });
    }
    return;
}

function isAllowedArgument(string argument) returns boolean {
    return ALLOWED_ARGUMENTS.indexOf(argument) != ();
}

// The compliant examples from the rule documentation
public function listDirectory(string userInput) returns os:Process|error {
    if !["reports", "archive"].some(directory => directory == userInput) {
        return error("unknown directory");
    }
    return check os:exec({value: "/bin/ls", arguments: [userInput]});
}

public function execCommand(string input) returns error? {
    string terminalPath = "/usr/bin/bal";
    string[] cmd = ["run", input];
    string[] allowed = ["main", "main.bal", "bal"];

    if allowed.filter(keyword => keyword.equalsIgnoreCaseAscii(input)).length() > 0 {
        os:Process result = check os:exec({
            value: terminalPath,
            arguments: cmd
        });
        _ = check result.waitForExit();
    }
}
