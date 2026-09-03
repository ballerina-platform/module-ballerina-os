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

// Resolved through PATH, so the environment decides which program runs
public function unqualifiedCommand() returns os:Process|error {
    return check os:exec({
        value: "ls",
        arguments: ["-la"]
    });
}

// The same through a variable
public function unqualifiedCommandViaVariable() returns os:Process|error {
    string executable = "git";
    return check os:exec({
        value: executable,
        arguments: ["status"]
    });
}

// Negative case - an absolute path
public function absolutePathCommand() returns os:Process|error {
    return check os:exec({
        value: "/usr/bin/git",
        arguments: ["status"]
    });
}

// Negative case - a relative path is still an explicit path
public function relativePathCommand() returns os:Process|error {
    return check os:exec({
        value: "./scripts/build.sh",
        arguments: []
    });
}
