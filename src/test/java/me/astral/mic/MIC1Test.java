package me.astral.mic;

import me.astral.mal.MAL;
import me.astral.mal.model.MALProgram;
import me.astral.mal.writer.MALWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MIC1Test {

    public static String EXPECTED_OUTPUT =     "                                                                                                    \n" +
            "                                                                                                    \n" +
            "                                                                                                    \n" +
            "                                                                         t`                         \n" +
            "                                                                         `':``'t@                   \n" +
            "                                                                        ``':L:``                    \n" +
            "                                                                     `t:':C@@8t'``                  \n" +
            "                                                                    `'f@@@@@@@@@@L`                 \n" +
            "                                                                   ``0@@@@@@@@@@@'`                 \n" +
            "                                                       ::`'8'```''''':tf@@@@@@@C:'''`'``      ``    \n" +
            "                                                       `fG@@G@::f@@@@@@@@@@@@@@@@@@@G@8:'`L''':'`   \n" +
            "                                                      ``'@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f@@@@8:@   \n" +
            "                                                    ``:::@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f`    \n" +
            "                                                  ``:8@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@L``   \n" +
            "                             't``     `t``      ```'@L@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:8`\n" +
            "                             `:tGt0:'':fG0:C'````''tC@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@G` \n" +
            "                            ```:f@@@0@@@@@@@@Gt:::t@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@t'' \n" +
            "                          ```C:GC@@@@@@@@@@@@@@@@f8@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:'  \n" +
            "                       'C''::t0@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f'`  \n" +
            "         `          ```':L@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f`    \n" +
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@L:'`     \n" +
            "         `          ```':L@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f`    \n" +
            "                       'C''::t0@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f'`  \n" +
            "                          ```C:GC@@@@@@@@@@@@@@@@f8@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:'  \n" +
            "                            ```:f@@@0@@@@@@@@Gt:::t@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@t'' \n" +
            "                             `:tGt0:'':fG0:C'````''tC@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@G` \n" +
            "                             't``     `t``      ```'@L@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:8`\n" +
            "                                                  ``:8@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@L``   \n" +
            "                                                    ``:::@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f`    \n" +
            "                                                      ``'@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@f@@@@8:@   \n" +
            "                                                       `fG@@G@::f@@@@@@@@@@@@@@@@@@@G@8:'`L''':'`   \n" +
            "                                                       ::`'8'```''''':tf@@@@@@@C:'''`'``      ``    \n" +
            "                                                                   ``0@@@@@@@@@@@'`                 \n" +
            "                                                                    `'f@@@@@@@@@@L`                 \n" +
            "                                                                     `t:':C@@8t'``                  \n" +
            "                                                                        ``':L:``                    \n" +
            "                                                                         `':``'t@                   \n" +
            "                                                                         t`                         \n" +
            "                                                                                                    \n" +
            "                                                                                                    \n";

    @Test
    public void testMandel() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("mandelbread.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs, System.in, bos);
        MIC1Runner.run(machine);
        String result = bos.toString();
        assertEquals(EXPECTED_OUTPUT, result);
    }

    @Test
    public void testGOTO1() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("GOTO1.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);
        machine.clock();
        machine.clock();
        machine.clock();
        int mainMPC = machine.getMPC();
        MIC1Runner.step(machine, mainMPC);
        assertEquals(0x31, machine.getRegister(MIC1Machine.TOS));
        MIC1Runner.step(machine, mainMPC);
        MIC1Runner.step(machine, mainMPC);
        MIC1Runner.step(machine, mainMPC);
        assertEquals(0x33, machine.getRegister(MIC1Machine.TOS));
        MIC1Runner.step(machine, mainMPC);
    }

    @Test
    public void testGOTO2() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("GOTO2.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);

        machine.clock();
        machine.clock();
        machine.clock();
        int mainMPC = machine.getMPC();
        MIC1Runner.step(machine, mainMPC); //BIPUSH 0x33
        assertEquals(0x31, machine.getRegister(MIC1Machine.TOS));
        MIC1Runner.step(machine, mainMPC); //OUT
        MIC1Runner.step(machine, mainMPC); //GOTO L3
        MIC1Runner.step(machine, mainMPC); //BIPUSH 0x33
        assertEquals(0x33, machine.getRegister(MIC1Machine.TOS));
        //OUT
        machine.clock(); //Main1
        machine.clock(); //out1	OPC=H=-1
        machine.clock(); //OPC=H+OPC
        machine.clock(); //MAR=H+OPC			// compute OUT address
        machine.clock(); //MDR=TOS; wr			// write to output
        machine.clock(); //        nop
        machine.clock(); //MAR=SP=SP-1; rd                 // decrement stack pointer
        machine.clock(); //        nop
        machine.clock(); //TOS=MDR; goto Main1
        //GOTO L2
        machine.clock(); //Main1	PC = PC + 1; fetch; goto (MBR)	// MBR holds opcode; get next byte; dispatch
        machine.clock(); //goto1	OPC = PC - 1			// Save address of opcode.
        machine.clock(); //goto2	PC = PC + 1; fetch		// MBR = 1st byte of offset; fetch 2nd byte
        machine.clock(); //goto3	H = MBR << 8			// Shift and save signed first byte in H
        assertEquals(-256, machine.getRegister(MIC1Machine.H));
        machine.clock(); //goto4	H = MBRU OR H			// H = 16-bit branch offset
        assertEquals(-7, machine.getRegister(MIC1Machine.H));
        machine.clock(); //goto5	PC = OPC + H; fetch		// Add offset to OPC
        machine.clock(); //goto6	goto Main1			// Wait for fetch of next opcode
        MIC1Runner.step(machine, mainMPC); //BIPUSH 0x32
        assertEquals(0x32, machine.getRegister(MIC1Machine.TOS));
        MIC1Runner.step(machine, mainMPC); //OUT
        MIC1Runner.step(machine, mainMPC); //HALT
    }

    @Test
    public void testIFEQ1() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("IFEQ1.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);
        MIC1Runner.run(machine);
    }

    @Test
    public void testIFICMPEQ1() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("IFICMPEQ1.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);
        MIC1Runner.run(machine);
    }

    @Test
    public void testIFLT1() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("IFLT1.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);
        MIC1Runner.run(machine);
    }

    @Test
    public void testSimpleIADD() throws Exception{
        /*
            BIPUSH 0x70
            BIPUSH 0x70
            IADD
         */
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("simple_add.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                        .getResourceAsStream("example.mal")
                        .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();
        System.out.println(dumpControlStore(cs));
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);

        machine.clock(); //NOP
        assertEquals(-1, machine.getRegister(MIC1Machine.PC));
        assertEquals(0, machine.getRegister(MIC1Machine.MBR));

        machine.clock(); //Main1
        assertEquals(0, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(0, machine.getRegister(MIC1Machine.MBR));
        assertEquals(0, machine.getMPC()); //Next address should be again 0 -> NOP

        machine.clock(); //NOP
        assertEquals(0, machine.getRegister(MIC1Machine.PC));
        assertEquals(16, machine.getRegister(MIC1Machine.MBR)); //First bytecode is read

        int mainMPC = machine.getMPC();

        machine.clock(); //Main1
        assertEquals(1, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(16, machine.getRegister(MIC1Machine.MBR));
        assertEquals(16, machine.getMPC()); //Next address should be MBR

        machine.clock(); //bipush1	SP = MAR = SP + 1
        assertEquals(MIC1Machine.BASE_SP + 1, machine.getRegister(MIC1Machine.SP)); //SP = MAR = SP + 1
        assertEquals(MIC1Machine.BASE_SP + 1, machine.getRegister(MIC1Machine.MAR)); //SP = MAR = SP + 1

        machine.clock(); //bipush2	PC = PC + 1; fetch
        assertEquals(2, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(0x70, machine.getRegister(MIC1Machine.MBR)); //fetch from Main1

        machine.clock(); //bipush3	MDR = TOS = MBR; wr; goto Main1
        assertEquals(0x70, machine.getRegister(MIC1Machine.TOS)); //MDR = TOS = MBR
        assertEquals(0x70, machine.getRegister(MIC1Machine.MDR));
        assertEquals(mainMPC, machine.getMPC()); //goto Main1

        machine.clock(); //Main1	PC = PC + 1; fetch; goto (MBR)
        assertEquals(3, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(16, machine.getRegister(MIC1Machine.MBR)); //read from bipush2
        assertEquals(16, machine.getMPC());

        machine.clock(); //bipush1	SP = MAR = SP + 1
        assertEquals(MIC1Machine.BASE_SP + 2, machine.getRegister(MIC1Machine.SP)); //SP = MAR = SP + 1
        assertEquals(MIC1Machine.BASE_SP + 2, machine.getRegister(MIC1Machine.MAR)); //SP = MAR = SP + 1

        machine.clock(); //bipush2	PC = PC + 1; fetch
        assertEquals(4, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(0x70, machine.getRegister(MIC1Machine.MBR)); //fetch from Main1

        machine.clock(); //bipush3	MDR = TOS = MBR; wr; goto Main1
        assertEquals(0x70, machine.getRegister(MIC1Machine.TOS)); //MDR = TOS = MBR
        assertEquals(0x70, machine.getRegister(MIC1Machine.MDR));
        assertEquals(mainMPC, machine.getMPC()); //goto Main1

        machine.clock(); //Main1	PC = PC + 1; fetch; goto (MBR)
        assertEquals(5, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(0x60, machine.getRegister(MIC1Machine.MBR)); //read from bipush2
        assertEquals(0x60, machine.getMPC()); //next op iadd1

        machine.clock(); //iadd1	MAR = SP = SP - 1; rd
        assertEquals(MIC1Machine.BASE_SP + 1, machine.getRegister(MIC1Machine.SP)); //SP = MAR = SP + 1
        assertEquals(MIC1Machine.BASE_SP + 1, machine.getRegister(MIC1Machine.MAR)); //SP = MAR = SP + 1
        assertEquals(0xFF, machine.getRegister(MIC1Machine.MBRU)); //fetch from Main1

        machine.clock(); //iadd2	H = TOS
        assertEquals(0x70, machine.getRegister(MIC1Machine.H));
        assertEquals(0x70, machine.getRegister(MIC1Machine.MDR)); //read from iadd1

        machine.clock(); //iadd3	MDR = TOS = MDR + H; wr; goto Main1
        assertEquals(0x70 + 0x70, machine.getRegister(MIC1Machine.MDR));
        assertEquals(0x70 + 0x70, machine.getRegister(MIC1Machine.TOS));

        machine.clock(); //Main1	PC = PC + 1; fetch; goto (MBR)
        assertEquals(6, machine.getRegister(MIC1Machine.PC)); //PC = PC + 1
        assertEquals(0xFF, machine.getMPC()); //next op halt1

        machine.clock();
        assertEquals(0xFF, machine.getMPC());

        assertTrue(machine.isHalted());
    }


    @Test
    public void testComplexProgram() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("14.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("example.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs, InputStream.nullInputStream(), bos);
        MIC1Runner.run(machine);

        String output = bos.toString();
        assertEquals("4", output);
    }

    @Test
    public void testSDUP() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("sdup.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("sdup.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();

        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);
        for(int i = 0; i < 12; i++) machine.clock();

        int sp = machine.getRegister(MIC1Machine.SP);
        assertEquals(5, machine.getMPC());
        assertEquals(0x70, machine.getRegister(MIC1Machine.TOS));

        machine.clock();
        machine.clock();

        assertEquals(0x70, machine.getRegister(MIC1Machine.H));
        assertEquals(0x70, machine.getRegister(MIC1Machine.MDR));

        machine.clock();

        assertTrue(machine.isZ());

        machine.clock();
        assertEquals(sp - 1, machine.getRegister(MIC1Machine.SP));
    }

    @Test
    public void testSDUPAllCases() throws Exception{
        byte[] ijvmProgram = MIC1Test.class.getClassLoader()
                .getResourceAsStream("sdup_all_tests.ijvm")
                .readAllBytes();

        byte[] controlCodeSource = MIC1Test.class.getClassLoader()
                .getResourceAsStream("sdup.mal")
                .readAllBytes();

        MALProgram program = MAL.parse(new String(controlCodeSource));

        byte[] cs = new MALWriter(program).write();

        MIC1Machine machine = MIC1Runner.loadIJVM(ijvmProgram, cs);

        machine.clock();
        int mainAddress = machine.getMPC();

        while (!machine.isHalted()){
            while (machine.getMPC() != 5 && !machine.isHalted()) {
                machine.clock();
            }
            if (machine.isHalted())
                break;
            machine.printStack();
            while (machine.getMPC() != mainAddress) machine.clock();
            machine.printStack();
            System.out.println("--------------------------");
        }

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