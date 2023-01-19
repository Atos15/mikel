package me.astral.mal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MALRegisters {
    MAR(true, null),
    MDR(true, ALUInput.B),
    PC(true, ALUInput.B),
    MBR(false, ALUInput.B),
    MBRU(false, ALUInput.B),
    SP(true, ALUInput.B),
    LV(true, ALUInput.B),
    CPP(true, ALUInput.B),
    TOS(true, ALUInput.B),
    OPC(true, ALUInput.B),
    H(true, ALUInput.A),
    Z(true, null),
    N(true, null);

    private final boolean writable;
    private final ALUInput aluInput;

    public boolean isReadable(){
        return aluInput != null;
    }
}
