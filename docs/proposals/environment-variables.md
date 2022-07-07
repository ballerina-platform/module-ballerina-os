# Proposal: OS Environment Variables related Operations

_Owners_: @daneshk @MadhukaHarith92  
_Reviewers_: @daneshk  
_Created_: 2022/02/18   
_Updated_: 2022/02/18  
_Issues_: [#2666](https://github.com/ballerina-platform/ballerina-standard-library/issues/2666)  [#2630](https://github.com/ballerina-platform/ballerina-standard-library/issues/2630)

## Summary
At the moment, the Ballerina OS module only has APIs to retrieve environment variable information. However, it’s not possible to perform other operations such as setting new environment variables, listing them, and unsettling particular environment variables. In this proposal, we describe how these requirements can be fascilitated.

## Goals
To support setting, unsettling, and listing environment variables.

## Motivation
To allow users to manipulate environment variables programmatically.

## Description

```ballerina
# Represents OS module related errors.
public type Error distinct error;
```

- The `setEnv` function can be used to set any environment variable programmatically. An `os:Error` will be returned if the `key` is an empty string, an initial hexadecimal zero character (0x00), or an equal sign ("="). An `os:Error` will also be returned  if a security exception occurs, i.e. the caller does not have the required permission to perform this operation, or an error occurs during the execution of this operation. 

```ballerina
# Sets the value of the environment variable named by the key. 
# ```ballerina
# os:Error? err = os:setEnv("BALCONFIGFILE", "/path/to/Config.toml");
# ```
#
# + key - Key of the environment variable
# + value - Value of the environment variable
# + return - error if setting the environment variable fails, () otherwise
public isolated function setEnv(string key, string value) returns Error?;
```

- Users can remove any particular environment variable from the system using the `unsetEnv` function. If a key of a non-existing environment variable is passed, no change will happen and an error will not be thrown. An `os:Error` will be returned if a security exception occurs, i.e. the caller does not have the required permission to perform this operation, or an error occurs during the execution of this operation.

```ballerina
# Removes a single environment variable from the system if it exists.
# ```ballerina
# os:Error? err = os:unsetEnv("BALCONFIGFILE");
# ```
#
# + name - Name of the environment variable
# + return - error if unsetting the environment variable fails, () otherwise
public isolated function unsetEnv(string key) returns Error?;
```

- To list the existing environment variables of the system, the `listEnv` function can be used. This would return the environment variables as a map.

```ballerina
# Returns a map of environment variables.
# ```ballerina
# map<string> envs = os:listEnv();
# ```
#
# + return - map of environment variables
public isolated function listEnv() returns map<string>;
```

## Testing
- Set an environment variable using the `setEnv` function and validate if it was set correctly.
- Pass an invalid variable (an empty string, an initial hexadecimal zero character (0x00), or an equal sign ("=")) as key to `setEnv` and validate if it returned an error correctly.
- Remove the previously created environment variable using the `unsetEnv` function can check whether it had been removed from the system correctly.
- Pass a non-existing key to `unsetEnv` and validate that it did not return an error.
- Retrieve the list of environment variables by calling the `listEnv` function and check whether this returns a valid `map<string>`. 
