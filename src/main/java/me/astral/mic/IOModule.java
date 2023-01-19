package me.astral.mic;

import java.io.InputStream;
import java.io.OutputStream;

public interface IOModule {

    void output(int data);
    int input();

    default int get32(int wordAddress){
        int byteAddress = wordAddress << 2;
        return ((get8Unsigned(byteAddress) << 24) +
                (get8Unsigned(byteAddress + 1) << 16) +
                (get8Unsigned(byteAddress + 2) << 8) +
                (get8Unsigned(byteAddress + 3)));
    };
    default void set32(int wordAddress, int value){
        int byteAddress = wordAddress << 2;

        set8(byteAddress, (byte) ((value >>> 24) & 0xFF));
        set8(byteAddress + 1, (byte) ((value >>> 16) & 0xFF));
        set8(byteAddress + 2, (byte) ((value >>> 8) & 0xFF));
        set8(byteAddress + 3, (byte) ((value) & 0xFF));
    };

    byte get8(int byteAddress);
    void set8(int byteAddress, byte value);

    void clear();

    default int get8Unsigned(int byteAddress){
        return Byte.toUnsignedInt(get8(byteAddress));
    };



}
