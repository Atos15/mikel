package me.astral.mic;

import me.astral.mic.model.ALU;
import me.astral.mic.model.BusCControl;

import java.util.Arrays;
import java.util.BitSet;

public class MIC1Machine {

    private final MIC1Instruction[] instructions = new MIC1Instruction[512];
    private final IOModule memory;

    private int MPC = 0;
    private MIC1Instruction currentInstruction; //MIR

    private final ALU alu = new ALU();

    public static final int BASE_SP = 0x8000;
    public static final int BASE_CPP = 0x4000;
    public static final int BASE_LV = 0xc000;
    public static final int MDR = 0;
    public static final int PC = 1;
    public static final int MBR = 2;
    public static final int MBRU = 3;
    public static final int SP = 4;
    public static final int LV = 5;
    public static final int CPP = 6;
    public static final int TOS = 7;
    public static final int OPC = 8;

    public static final int H = 9;
    public static final int MAR = 10;

    private final int[] registers = new int[11];

    private int busC = 0;
    private boolean N = false;
    private boolean Z = false;

    private boolean toRead = false;
    private boolean toWrite = false;
    private boolean toFetch = false;

    private boolean halted = false;

    public MIC1Machine(IOModule module){
        this.memory = module;
        reset();
    }

    public MIC1Machine(){
        this(new DefaultIOModule(System.in, System.out));
    }

    public void loadMicrocode(byte[] microcode){
        BitSet bitSet = BitSet.valueOf(microcode);

        for (int i = 0; i < 512 * 36 / 8; i++){
            int byteIndex = i * 8;

            for (int j = 0; j < 4; j++){
                boolean left = bitSet.get(byteIndex + j);
                boolean right = bitSet.get(byteIndex + (7 - j));
                bitSet.set(byteIndex + j, right);
                bitSet.set(byteIndex + (7 - j), left);
            }
        }

        for (int i = 0; i < 512; i++){
            instructions[i] = MIC1Instruction.fromBytes(bitSet, i);
        }
    }

    public void reset(){
        Arrays.fill(registers, 0);

        registers[SP] = BASE_SP;
        registers[CPP] = BASE_CPP;
        registers[LV] = BASE_LV;
        registers[PC] = -1;

        toRead = false;
        toWrite = false;
        toFetch = false;
        Z = false;
        N = false;
    }

    public void clock(){
        //Subcycle 1 - Load MIR

        currentInstruction = instructions[MPC];

        //Subcycle 2 - Drive H and B bus
        driveBusToALU();

        //Subcycle 3 - Run ALU and SHIFTER and
        runALU();
        runShifter();

        //Subcycle 4 - Propagate to bus

        //Rising Edge - Handle IO, write to registers, update MPC and initialize new IO
        handleFetch();
        handleMemoryIO();

        writeToRegisters();
        updateJumpFlags();

        updateMPC();
        initializeMemoryIO();
    }


    private void fetch(){
        byte value = memory.get8(registers[PC]);
        registers[MBR] = value;
        registers[MBRU] =  Byte.toUnsignedInt(value);
    }

    private void handleFetch(){
        if (toFetch) {
            fetch();
            toFetch = false;
        }
    }

    private void handleMemoryIO(){
        if (toRead){
            int value = registers[MAR] < 0 ? memory.input() : memory.get32(registers[MAR]);
            registers[MDR] = value;
            toRead = false;
        }else if (toWrite){
            if (registers[MAR] < 0)
                memory.output(registers[MDR]);
            else
                memory.set32(registers[MAR], registers[MDR]);
            toWrite = false;
        }
    }

    private void writeToRegisters(){
        BusCControl busCControl = currentInstruction.busCControl();
        if (busCControl.enableH())
            registers[H] = busC;
        if (busCControl.enableOPC())
            registers[OPC] = busC;
        if (busCControl.enableTOS())
            registers[TOS] = busC;
        if (busCControl.enableCPP())
            registers[CPP] = busC;
        if (busCControl.enableLV())
            registers[LV] = busC;
        if (busCControl.enableSP())
            registers[SP] = busC;
        if (busCControl.enablePC())
            registers[PC] = busC;
        if (busCControl.enableMDR())
            registers[MDR] = busC;
        if (busCControl.enableMAR())
            registers[MAR] = busC;
    }

    private void updateJumpFlags(){
        N = busC < 0;
        Z = busC == 0;
    }

    private void runShifter(){
        if (currentInstruction.shifterControl().ssl8()){
            busC = busC << 8;
        }else if (currentInstruction.shifterControl().sra1()){
            busC = busC >> 1;
        }
    }

    private void runALU(){
        alu.setControl(currentInstruction.aluControl());
        alu.clock();
        busC = alu.getOutput();
    }

    private void initializeMemoryIO(){
        switch (currentInstruction.memory()){
            case READ -> toRead = true;
            case WRITE -> toWrite = true;
        }
        if (currentInstruction.fetch()){
            toFetch = true;
        }
    }

    private void updateMPC(){
        int old = MPC;

        MPC = switch (currentInstruction.jam()){
            case JMPC -> currentInstruction.nextAddress() | registers[MBRU];
            case JAMN -> currentInstruction.nextAddress() + 256 * (N ? 1 : 0);
            case JAMZ -> currentInstruction.nextAddress() + 256 * (Z ? 1 : 0);
            case NONE -> currentInstruction.nextAddress();
        };

        halted = old == MPC;
    }

    private void driveBusToALU(){
        int busAValue = registers[H];

        int registerIndex = currentInstruction.bus();
        int busBValue = isValidBusBRegister(registerIndex)  ? registers[registerIndex] : 0;


        alu.setBusA(busAValue);
        alu.setBusB(busBValue);
    }

    private boolean isValidBusBRegister(int index){
        return index >= 0 && index < 9;
    }

    public boolean isHalted() {
        return halted;
    }

    public int getRegister(int index){
        return registers[index];
    }

    public int getMPC(){
        return MPC;
    }

    public boolean isN() {
        return N;
    }

    public boolean isZ() {
        return Z;
    }

    public boolean isToFetch() {
        return toFetch;
    }

    public boolean isToRead() {
        return toRead;
    }

    public boolean isToWrite() {
        return toWrite;
    }

    public MIC1Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public void printStack(){
        StringBuilder stack = new StringBuilder("[");
        for (int i = BASE_SP; i < registers[SP]; i++){
            stack.append(memory.get32(i));
            stack.append(", ");
        }
        if (stack.length() > 1)
            stack.setLength(stack.length() - 2);
        stack.append("]");
        System.out.println(stack);
    }
}
