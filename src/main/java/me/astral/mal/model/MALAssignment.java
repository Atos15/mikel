package me.astral.mal.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MALAssignment {
    private List<MALRegisters> target;
    private MALOperations operation;
    private List<MALRegisters> operand;
    private MALExpressionModifier modifier;
}
