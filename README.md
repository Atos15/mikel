# mikel
A MIC-1 simulator written in Java 17

## Download



## Getting Started

### Requirements
1. Java 17

### Run Command
To run a IJVM program you need the following:
1. A compiled .ijvm file
2. A compiled (or textual) MAL file

Examples of those files can be found here.

```shell{:copy}
java -jar --enable-preview .\mikel.jar run -m PATH_IJVM PATH_MAL
```

{% note %}

**Note:** the -m flag enables textual MAL mode, as such the MAL file is expected to be textual

{% endnote %}