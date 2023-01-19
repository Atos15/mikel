package me.astral.mic;

import me.astral.mic.model.ALUControl;
import me.astral.mic.model.BusCControl;
import me.astral.mic.model.ShifterControl;

import java.util.BitSet;

public record MIC1Instruction(
        short nextAddress,
        JAM jam,
        ShifterControl shifterControl,
        ALUControl aluControl,
        BusCControl busCControl,
        MemoryFlag memory,
        boolean fetch,
        byte bus
        ) {

    public static MIC1Instruction fromBytes(BitSet set, int address){
        int bitOffset = address * 36;
        return new MIC1Instruction(
                readAddress(set, bitOffset),
                readJAM(set, bitOffset),
                readShifterControl(set, bitOffset),
                readAluControl(set, bitOffset),
                reaBusCControl(set, bitOffset),
                readMemory(set, bitOffset),
                readFetch(set, bitOffset),
                readBus(set, bitOffset)
        );
    }

    private static ShifterControl readShifterControl(BitSet set, int bitOffset){
        return new ShifterControl(
                set.get(bitOffset + 12),
                set.get(bitOffset + 13)
        );
    }

    private static ALUControl readAluControl(BitSet set,  int bitOffset){
        return new ALUControl(
                set.get(bitOffset + 14),
                set.get(bitOffset + 15),
                set.get(bitOffset + 16),
                set.get(bitOffset + 17),
                set.get(bitOffset + 18),
                set.get(bitOffset + 19)
        );
    }

    private static BusCControl reaBusCControl(BitSet set, int bitOffset){
        return new BusCControl(
                set.get(bitOffset + 20),
                set.get(bitOffset + 21),
                set.get(bitOffset + 22),
                set.get(bitOffset + 23),
                set.get(bitOffset + 24),
                set.get(bitOffset + 25),
                set.get(bitOffset + 26),
                set.get(bitOffset + 27),
                set.get(bitOffset + 28)
        );
    }

    private static short readAddress(BitSet set, int bitOffset){
        return (short) ((readNumeric(set, bitOffset, 9) & 0xFFFF));
    }

    private static long readNumeric(BitSet set, int bitOffset, int length){
        long value = 0;

        for (int i = 0; i < length; i++){
            value += ((set.get(bitOffset + i) ? 1L : 0L) << (length - 1 - i));
        }

        return value;
    }

    private static byte readBus(BitSet set, int bitOffset){
        return (byte) ((readNumeric(set, bitOffset + 32, 4) & 0xFF));
    }

    private static JAM readJAM(BitSet set, int bitOffset){
        if (set.get(bitOffset + 9))
            return JAM.JMPC;
        if (set.get(bitOffset + 10))
            return JAM.JAMN;
        if (set.get(bitOffset + 11))
            return JAM.JAMZ;
        return JAM.NONE;
    }

    private static MemoryFlag readMemory(BitSet set, int bitOffset){
        if (set.get(bitOffset + 29))
            return MemoryFlag.WRITE;
        if (set.get(bitOffset + 30))
            return MemoryFlag.READ;
        return MemoryFlag.NONE;
    }

    private static boolean readFetch(BitSet set, int bitOffset){
        return set.get(bitOffset + 31);
    }
}
