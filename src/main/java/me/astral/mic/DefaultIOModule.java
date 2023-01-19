package me.astral.mic;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultIOModule implements IOModule {

    private Map<Integer, Byte> memory = new HashMap<>();

    private final InputStream inputStream;
    private final OutputStream outputStream;

    @Override
    public byte get8(int address) {
        return memory.getOrDefault(address, (byte) 0);
    }

    @Override
    public void set8(int address, byte value) {
        memory.put(address, value);
    }

    @Override
    public void clear() {
        this.memory.clear();
    }

    @Override
    public void output(int data) {
        try{
            this.outputStream.write(data & 0xFF);
            this.outputStream.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public int input() {
        try {
            return this.inputStream.read();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
