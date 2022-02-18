# Proposal: OS Environment Variables related Operations

_Owners_: @daneshk @MadhukaHarith92  
_Reviewers_: @daneshk  
_Created_: 2022/12/18   
_Updated_: 2022/12/18  
_Issues_: [#2666](https://github.com/ballerina-platform/ballerina-standard-library/issues/2666)  [#2630](https://github.com/ballerina-platform/ballerina-standard-library/issues/2630)

## Summary
At the moment, the Ballerina OS module only has APIs to retrieve environment variable information. However, it’s not possible to perform other operations such as setting new environment variables, listing them, and unsettling particular environment variables. In this proposal, we describe how these requirements can be fascilitated.

## Goals
To support setting, unsettling, and listing environment variables.

## Motivation
To allow users to manipulate environment variables programmatically.

## Description
- The `setEnv` function can be used to set any environment variable programmatically.

```ballerina
# Sets the value of the environment variable named by the key. 
# ```ballerina
# os:Error err = os:setEnv("BALCONFIGFILE", "/path/to/Config.toml");
# ```
#
# + key - Key of the environment variable
# + value - Value of the environment variable
# + return - error if setting the environment variable fails
public isolated function setEnv(string key, string value) returns Error;
```

- Users can remove any particular environment variable from the system using the `unsetEnv` function.

```ballerina
# Removes a single environment variable from the system if it exists.
# ```ballerina
# var env = os:unsetEnv("BALCONFIGFILE");
# ```
#
# + name - Name of the environment variable
public isolated function unsetEnv(string key);
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
- Remove the previously created environment variable using the `unsetEnv` function can check whether it had been removed from the system correctly.
- Retrieve the list of environment variables by calling the `listEnv` function and check whether this returns a valid `map<string>`. 
