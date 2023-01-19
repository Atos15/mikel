package me.astral.mal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class MALIfStatement extends MALControlStatement {
    private final MALRegisters condition;
    private final String truelabel;
    private final String falseLabel;
}
