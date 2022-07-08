# Proposal: OS Command Execution

_Owners_: @daneshk @MadhukaHarith92  
_Reviewers_: @daneshk  
_Created_: 2022/07/07   
_Updated_: 2022/07/07  
_Issues_: [#2963](https://github.com/ballerina-platform/ballerina-standard-library/issues/2963)

## Summary
At the moment, the Ballerina OS module does not have an API to execute OS commands. In this proposal, we describe how this requirement can be facilitated.

## Goals
To support OS command execution.

## Motivation
To allow users to execute operating system commands programmatically.

## Description

```ballerina
# Represents a command.
#
# + value - The value of the command
# + arguments - The arguments of the command
public type Command record {
    string value;
    string[] arguments;
};

# Executes an operating system command as a subprocess of the current process.
# ```ballerina
# os:Process|os:Error result = os:exec({value: "bal", arguments: ["run", filepath]}, BAL_CONFIG_FILE = "/abc/Config.toml");
# ```
#
# + command - The command to be executed
# + envProperties - The environment properties
#
# + return - Process object in success, or an Error if a failure occurs
public isolated function  exec(Command command, *EnvProperties envProperties) returns Process | Error;
```
- The `exec` function can be used to execute OS commands programmatically. Users can pass the command as a raw template. Additionally, users can pass any number of environment properties as key-value pairs that need to be set when executing the command.

```ballerina
# Environment properties that need to be set when executing the command.
#
# + command - command which cannot be a key
public type EnvProperties record {|
    never command?;
    anydata...;
|};
```

- Represents an environment property as an included record type.

```ballerina
# This object contains information on a process being created from Ballerina.
# This is returned from the `exec` function.
public class Process {

    # Waits for the process to finish its work and exit.
    # ```ballerina
    # int|os:Error exitCode = process.waitForExit();
    # ```
    #
    # + return - Returns the exit code for the process, or else an `Error` if a failure occurs
    public isolated function waitForExit() returns int|Error;

    # Provides a stream (to read from), which is made available as the 'standard error', or the 'standard out' of the process.
    # ```ballerina
    # byte[]|os:Error err = process.output(io:stderr);
    # ```
    #
    # + return - The `byte[]`, which represents the process's 'standard error', or the 'standard out', or an Error
    public isolated function output(io:FileOutputStream fileOutputStream) returns byte[]|Error;

    # Terminates the process.
    # ```ballerina
    # process.exit();
    # ```
    #
    public isolated function exit();
}
```

## Testing
- Execute an OS command using the `exec` function and validate whether it was successful.
- Pass an environment property to the `exec` function and validate if it was set correctly.
