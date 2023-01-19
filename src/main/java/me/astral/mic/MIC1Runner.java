package me.astral.mic;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

public class MIC1Runner {

    public static void step(MIC1Machine machine, int mainAddress){
        while(!machine.isHalted()){
            machine.clock();
            if (machine.getMPC() == mainAddress)
                return;
        }
    }

    public static void run(MIC1Machine machine){
        while(!machine.isHalted()){
            machine.clock();
        }
    }

    public static void runIJVM(byte[] program, byte[] controlStore){
        MIC1Machine machine = loadIJVM(program, controlStore);
        run(machine);
    }

    public static MIC1Machine loadIJVM(byte[] program, byte[] controlStore){
        DefaultIOModule memoryModule = new DefaultIOModule(System.in, System.out);
        loadProgram(program, memoryModule);
        MIC1Machine machine = new MIC1Machine(memoryModule);
        machine.loadMicrocode(controlStore);
        return machine;
    }

    public static MIC1Machine loadIJVM(byte[] program, byte[] controlStore, InputStream is, OutputStream os){
        DefaultIOModule memoryModule = new DefaultIOModule(is, os);
        loadProgram(program, memoryModule);
        MIC1Machine machine = new MIC1Machine(memoryModule);
        machine.loadMicrocode(controlStore);
        return machine;
    }

    private static void loadProgram(byte[] ijvmProgram, DefaultIOModule memoryModule){
        ByteBuffer buffer = ByteBuffer.wrap(ijvmProgram);
        int magicNumber = buffer.getInt();
        if (magicNumber != 0x1DEADFAD)
            throw new IllegalArgumentException("Program is not IJVM binary");

        int constantPoolOrigin = buffer.getInt();
        if (constantPoolOrigin != 0x00010000)
            throw new IllegalArgumentException("Constant Pool Origin must be 0x00010000, but found " + constantPoolOrigin);

        int constantPoolSize = buffer.getInt();
        for (int i = 0; i < constantPoolSize; i++){
            memoryModule.set8(constantPoolOrigin + i, buffer.get());
        }

        int textOrigin = buffer.getInt();
        if (textOrigin != 0)
            throw new IllegalArgumentException("Text Origin must be 0, but found " + textOrigin);

        int textSize = buffer.getInt();
        for (int i = 0; i < textSize; i++){
            memoryModule.set8(textOrigin + i, buffer.get());
        }
    }

}
