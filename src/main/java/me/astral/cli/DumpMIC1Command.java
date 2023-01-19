package me.astral.cli;

import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "dump")
public class DumpMIC1Command implements Callable<Integer> {
    @CommandLine.Parameters(paramLabel = "INPUT", description = "The binary control store (.mic1)")
    private File mic1file;

    @Override
    public Integer call() throws Exception {
        String dump = dumpControlStore(Files.readAllBytes(mic1file.toPath()));
        System.out.println(dump);
        return 0;
    }


    private static String dumpControlStore(byte[] cs){
        BitSet bitSet = BitSet.valueOf(cs);
        StringBuilder builder = new StringBuilder();
        int[][] ranges = {{0, 9, 1}, {9, 12, 0}, {12, 14, 0}, {14, 20, 0}, {20, 29, 0}, {29, 32, 0}, {32, 36, 0}};
        for (int i = 0; i < 512; i++){
            int bitAddress = i * 36;
            String address = "0x" + padLeft(Integer.toHexString(i).toUpperCase(), 3);

            builder.append(address);
            builder.append(": ");

            for (int[] range : ranges){
                String bits = getBits(bitSet, bitAddress + range[0], bitAddress + range[1]);

                if (range[2] == 1){
                    int value = Integer.parseInt(bits, 2);
                    bits = "0x" + padLeft(Integer.toHexString(value).toUpperCase(), 3);
                }

                builder.append(bits);
                builder.append(" ");
            }
            builder.setLength(builder.length() - 1);
            builder.append('\n');
        }
        return builder.toString();
    }

    private static String getBits(BitSet set, int from, int to){
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < to; i++){
            builder.append(getBit(set, i));
        }
        return builder.toString();
    }

    private static int getBit(BitSet set, int index){
        int byteIndex = index / 8;
        int bitIndex = index - byteIndex * 8;
        return set.get(byteIndex * 8  + (7 - bitIndex)) ? 1 : 0;
    }

    private static String padLeft(String string, int length){
        if (string.length() >= length) {
            return string;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - string.length()) {
            sb.append('0');
        }
        sb.append(string);

        return sb.toString();
    }
}
