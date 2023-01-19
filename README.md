# mikel
A MIC-1 simulator written in Java 17

## Download

You can download the latest release [here](https://github.com/Atos15/mikel/tree/main/src/test/resources/examples).

## Getting Started

### Requirements
1. Java 17

### Run Command
To run a IJVM program you need the following:
1. A compiled .ijvm file
2. A compiled (or textual) MAL file

Examples of those files can be found [here](https://github.com/Atos15/mikel/tree/main/src/test/resources/examples).

```shell{:copy}
java -jar --enable-preview .\mikel.jar run -m PATH_IJVM PATH_MAL
```

**Note:** the -m flag enables textual MAL mode, as such the MAL file is expected to be textual.

### Assemble Command
To assemble a textual MAL file in to a binary one, the following command can be used:

```shell{:copy}
java -jar --enable-preview .\mikel.jar assemble INPUT_MAL OUPUT_MIC1
```

**Note:** Usually textual MAL files end with .mal, while binary ones end with .mic1

### Dump Command
The dump command can be used to inspect the content of a binary MAL file (.mic1).

```shell{:copy}
java -jar --enable-preview .\mikel.jar dump INPUT_MIC1
```

Each line of the output corresponds to a microinstruction.

A tipical instruction will look like this:

```
0x003: 0x042 000 00 110101 000000100 000 0001
```

| Address | Next Address | JUMP | SHIFTER | ALU | Bus C Control | Memory Control | Bus B Control |
|---------|--------------|------|---------|-----|---------------|----------------|---------------|
| 0x003:  |  0x042        | 000  | 00|110101|000000100|000|0001|


