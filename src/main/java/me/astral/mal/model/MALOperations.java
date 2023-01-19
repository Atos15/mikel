package me.astral.mal.model;

public enum MALOperations {
    IDENTITY, // H or SOURCE
    NOT, // H or SOURCE
    ADD, // H + SOURCE or SOURCE + H
    ADD_INC, // H + SOURCE + 1 or SOURCE + H + 1
    INC, // H + 1 or SOURCE + 1
    SUB, // SOURCE - H
    DEC, // SOURCE - 1
    NEGATE, // -H
    AND, // H AND SOURCE | SOURCE AND H
    OR,  // H OR SOURCE | SOURCE OR H
    CONSTANT_ONE,
    CONSTANT_ZERO,
    CONSTANT_MINUS_ONE// 0, 1, -1
}
