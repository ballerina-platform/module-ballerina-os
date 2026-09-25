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

// An absolute POSIX path
public function absolutePathCommand() returns os:Process|error {
    return check os:exec({
        value: "/usr/bin/git",
        arguments: ["status"]
    });
}

// The same through a variable
public function absolutePathCommandViaVariable() returns os:Process|error {
    string executable = "/usr/bin/git";
    return check os:exec({
        value: executable,
        arguments: ["status"]
    });
}

// A relative path is still an explicit path
public function relativePathCommand() returns os:Process|error {
    return check os:exec({
        value: "./scripts/build.sh",
        arguments: []
    });
}

// An absolute Windows path
public function windowsAbsolutePathCommand() returns os:Process|error {
    return check os:exec({
        value: "C:\\Program Files\\Git\\bin\\git.exe",
        arguments: ["status"]
    });
}

// A relative Windows path
public function windowsRelativePathCommand() returns os:Process|error {
    return check os:exec({
        value: ".\\tools\\build.exe",
        arguments: []
    });
}

// Windows resolves a drive-relative path against that drive's current directory
// rather than through PATH
public function driveRelativeCommand() returns os:Process|error {
    return check os:exec({
        value: "C:tool.exe",
        arguments: []
    });
}

// A bare name overwritten with an absolute path before the call
public function reassignedBeforeUse() returns os:Process|error {
    string executable = "git";
    executable = "/usr/bin/git";
    return check os:exec({
        value: executable,
        arguments: ["status"]
    });
}

// The compliant example from the rule documentation
public function status() returns os:Process|error {
    return check os:exec({value: "/usr/bin/git", arguments: ["status"]});
}
