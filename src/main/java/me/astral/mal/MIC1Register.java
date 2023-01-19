package me.astral.mal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum MIC1Register {

    MDR(0),
    PC(1),
    MBR(2),
    MBRU(3),
    SP(4),
    LV(5),
    CPP(6),
    TOS(7),
    OCP(8);

    private final int index;

    private static final Map<Integer, MIC1Register> indexToRegister;

    static {
        indexToRegister = new HashMap<>();

        for (MIC1Register register : values()){
            indexToRegister.put(register.getIndex(), register);
        }
    }

    MIC1Register(int index){
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static MIC1Register fromIndex(int index){
        return indexToRegister.get(index);
    }
}
