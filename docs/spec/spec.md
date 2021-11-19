# Specification: Ballerina OS Library

_Owners_: @daneshk @MadhukaHarith92  
_Reviewers_: @daneshk  
_Created_: 2021/11/10  
_Updated_: 2021/11/10  
_Issue_: [#2339](https://github.com/ballerina-platform/ballerina-standard-library/issues/2339)

# Introduction
The OS library is used to retrieve information about the operating system and its current users. It is part of Ballerina Standard Library. [Ballerina programming language](https://ballerina.io/) is an open-source programming language for the cloud that makes it easier to use, combine, and create network services.

# Contents

1. [Overview](#1-overview)
2. [Environment Variable Values](#2-environment-variable-values)
3. [Operating System Users Information](#3-operating-system-users-information)

## 1. Overview
This specification elaborates on the operating-system-related functions available in the OS library.

## 2. Environment Variable Values
The environment variable value associated with a provided name can be retrieved using the `os:getEnv()` function.
```ballerina
string port = os:getEnv("HTTP_PORT");
```
## 3. Operating System Users Information
The current user's name can be retrieved using the `os:getUsername()` function.
```ballerina
string username = os:getUsername();
```

The current user's home directory path can be retrieved using the `os:getUserHome()` function.
```ballerina
string userHome = os:getUserHome();
```
