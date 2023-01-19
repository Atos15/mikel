package me.astral.mal.writer;

import me.astral.mal.MALInstruction;
import me.astral.mal.model.*;
import me.astral.mic.JAM;
import me.astral.mic.MIC1Instruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class MALWriter {

    private static final int JMPC = 9;
    private static final int JAMN = 10;
    private static final int JAMZ = 11;

    private static final int SSL8 = 12;
    private static final int SRA1 = 13;

    private static final int F0 = 14;
    private static final int F1 = 15;
    private static final int ENA = 16;
    private static final int ENB = 17;
    private static final int INVA = 18;
    private static final int INC = 19;

    private static final int EN_H = 20;
    private static final int EN_OPC = 21;
    private static final int EN_TOS = 22;
    private static final int EN_CPP = 23;
    private static final int EN_LV = 24;
    private static final int EN_SP = 25;
    private static final int EN_PC = 26;
    private static final int EN_MDR = 27;
    private static final int EN_MAR = 28;

    private static final int WRITE = 29;
    private static final int READ = 30;
    private static final int FETCH = 31;


    private static final EnumMap<MALRegisters, Integer> REGISTER_ADDRESS = new EnumMap<>(MALRegisters.class);
    private static final int DEFAULT_REGISTER = 10;
    private static final int CONTROL_STORE_WORDS = 512;
    private static final int WORD_BIT_SIZE = 36;


    static {
        REGISTER_ADDRESS.put(MALRegisters.MDR, 0);
        REGISTER_ADDRESS.put(MALRegisters.PC, 1);
        REGISTER_ADDRESS.put(MALRegisters.MBR, 2);
        REGISTER_ADDRESS.put(MALRegisters.MBRU, 3);
        REGISTER_ADDRESS.put(MALRegisters.SP, 4);
        REGISTER_ADDRESS.put(MALRegisters.LV, 5);
        REGISTER_ADDRESS.put(MALRegisters.CPP, 6);
        REGISTER_ADDRESS.put(MALRegisters.TOS, 7);
        REGISTER_ADDRESS.put(MALRegisters.OPC, 8);
    }

    private final MALProgram program;
    private final BitSet usedWords = new BitSet(CONTROL_STORE_WORDS);
    private final BitSet controlStore = new BitSet(CONTROL_STORE_WORDS * WORD_BIT_SIZE);
    private final BitSet instructionEncoding = new BitSet(WORD_BIT_SIZE);

    private final Map<String, Integer> labelAddress = new HashMap<>();
    private final Map<Integer, Integer> instructionAddress = new HashMap<>();

    public MALWriter(MALProgram program){
        this.program = program;
    }

    public String debug(){
        StringBuilder stringBuilder = new StringBuilder();
        labelAddress.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach((e) -> {
            stringBuilder.append(String.format("%1$20s", e.getKey()));
            stringBuilder.append("\t");
            stringBuilder.append(e.getValue());
            stringBuilder.append('\n');
        });
        return stringBuilder.toString();
    }

    public byte[] write(){
        usedWords.clear();
        controlStore.clear();
        instructionEncoding.clear();
        labelAddress.clear();

        Map<String, Integer> anchoredLabels = new HashMap<>();

        // Map each anchored label to the corresponding address
        for (MALLabelDirective labels : program.labels()){
            int address = Integer.parseInt(labels.address().substring(2), 16);
            anchoredLabels.put(labels.label(), address);
            usedWords.set(address);
        }

        Set<String> handledLabels = new HashSet<>();

        //Assign a predefined address to any label that is part of an if statement
        //Due to the specific constraint of the MIC-1, the trueLabel's address must be
        //equal to the falseLabel's address + 256
        for (MALInstruction instruction : program.instructions()){
            MALControlStatement statement = instruction.controlStatement();
            if (!(statement instanceof MALIfStatement ifStatement))
                continue;
            String falseLabel = ifStatement.getFalseLabel();
            String trueLabel = ifStatement.getTruelabel();

            int falseAddress = anchoredLabels.containsKey(falseLabel) ?
                    anchoredLabels.get(falseLabel) :
                    getAvailableFalseAddress();
            int trueAddress = falseAddress + 256;

            labelAddress.put(falseLabel, falseAddress);
            usedWords.set(falseAddress);

            labelAddress.put(trueLabel, trueAddress);
            usedWords.set(trueAddress);

            handledLabels.add(falseLabel);
            handledLabels.add(trueLabel);
        }


        //Assign a predefined address to each labeled address, considering anchored labels and if-labels
        for (MALInstruction instruction : program.instructions()){
            String label = instruction.label();

            if (label == null || handledLabels.contains(label))
                continue;

            int address = 0;
            if (anchoredLabels.containsKey(label)){
                address = anchoredLabels.get(label);
            }else{
                address = usedWords.nextClearBit(0);
                usedWords.set(address, true);
            }

            labelAddress.put(label, address);
        }


        //Write all instruction to Control Store
        for (int i = 0; i < program.instructions().size(); i++){
            MALInstruction current = program.instructions().get(i);

            setCurrentInstruction(current);

            int currentAddress = getInstructionAddress(i);
            lockInstructionAddress(i, currentAddress);

            //If the instruction does not have a control statement, calculate next instruction
            if (current.controlStatement() == null && i + 1 < program.instructions().size()){
                int nextInstructionAddress = getInstructionAddress(i + 1);
                lockInstructionAddress(i + 1, nextInstructionAddress);
                setNextAddress(nextInstructionAddress);
            }

            writeInstruction(currentAddress);
        }


        //Fill remaining space with default directive
        if (program.defaultDirective() != null) {
            setCurrentInstruction(program.defaultDirective());
            int current = 0;
            while((current = usedWords.nextClearBit(current)) < CONTROL_STORE_WORDS){
                usedWords.set(current);
                writeInstruction(current);
            }
        }

        return toByteArray();
    }

    private byte[] toByteArray(){
        byte[] bytes = new byte[(CONTROL_STORE_WORDS / 8) * WORD_BIT_SIZE];

        int bitIndex = 0;
        while ((bitIndex = controlStore.nextSetBit(bitIndex + 1)) > 0){
            int byteIndex = bitIndex / 8;
            int bitOffset = bitIndex - byteIndex * 8;
            bytes[byteIndex] = (byte) ((bytes[byteIndex]  | (1 << (7 - bitOffset))) & 0xFF);
        }

        return bytes;
    }

    private void debugAll(){
        labelAddress
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach((e) -> System.out.println(e.getKey() + ": " + e.getValue()));
    }



    private int getInstructionAddress(int index){
        //If the specific instruction index has already been assigned an address, return it
        if (instructionAddress.containsKey(index))
            return instructionAddress.get(index);


        MALInstruction instruction = program.instructions().get(index);

        //If this instruction is labeled, return the corresponding label address
        if (instruction.label() != null)
            return labelAddress.get(instruction.label());

        //Otherwise, find the next available word address
        return usedWords.nextClearBit(0);
    }

    private void lockInstructionAddress(int index, int address){
        usedWords.set(address);
        instructionAddress.put(index, address);
    }


    private int getAvailableFalseAddress(){
        int falseAddress = -1;
        while(falseAddress < CONTROL_STORE_WORDS / 2){
            falseAddress = usedWords.nextClearBit(falseAddress + 1);
            int trueAddress = falseAddress + CONTROL_STORE_WORDS / 2;
            if (!usedWords.get(trueAddress))
                return falseAddress;
        }
        throw new IllegalStateException("Could not find an available false address");
    }


    private void setControl(MALControlStatement controlStatement){
         switch (controlStatement) {
            case MALIfStatement ifStatement -> {
                int nextAddress = labelAddress.get(ifStatement.getFalseLabel());
                setNextAddress(nextAddress);
                switch (ifStatement.getCondition()){
                    case N -> setJAM(JAM.JAMN);
                    case Z -> setJAM(JAM.JAMZ);
                }
            }
            case MALGotoStatement gotoStatement -> {
                int nextAddress = labelAddress.get(gotoStatement.getLabel());
                setNextAddress(nextAddress);
            }
            case MALMultiWayBranchStatement branchStatement -> {
                if (branchStatement.getAddress() == null){
                    setNextAddress(0);
                }else{
                    int address = Integer.parseInt(branchStatement.getAddress().substring(2), 16);
                    setNextAddress(address);
                }
                setJAM(JAM.JMPC);
            }
             default -> {}
         };
    }

    private void setJAM(JAM jam){
        instructionEncoding.set(JMPC, jam == JAM.JMPC);
        instructionEncoding.set(JAMN, jam == JAM.JAMN);
        instructionEncoding.set(JAMZ, jam == JAM.JAMZ);
    }

    private void setAssignment(MALAssignment assignment){
        switch (assignment.getOperation()){
            case IDENTITY -> {
                boolean useBusA = assignment.getOperand().contains(MALRegisters.H);
                setALUControl(useBusA ? 0b01_10_00 : 0b01_01_00);
            }
            case NOT -> {
                boolean useBusA = assignment.getOperand().contains(MALRegisters.H);
                setALUControl(useBusA ? 0b01_10_10 : 0b10_11_00);
            }
            case ADD -> setALUControl(0b11_11_00);
            case ADD_INC -> setALUControl(0b11_11_01);
            case INC -> {
                boolean useBusA = assignment.getOperand().contains(MALRegisters.H);
                setALUControl(useBusA ? 0b11_10_01 : 0b11_01_01);
            }
            case SUB -> setALUControl(0b11_11_11);
            case DEC -> setALUControl(0b11_01_11);
            case NEGATE -> setALUControl(0b10_11_11);
            case AND -> setALUControl(0b00_11_00);
            case OR -> setALUControl(0b01_11_00);
            case CONSTANT_ZERO -> setALUControl(0b01_00_00);
            case CONSTANT_ONE -> setALUControl(0b01_00_01);
            case CONSTANT_MINUS_ONE -> setALUControl(0b01_00_10);
        }

        if (assignment.getModifier() != null){
            switch (assignment.getModifier()){
                case LEFT_SHIFT_8 -> setShifterControl(true, false);
                case RIGHT_SHIFT_1 -> setShifterControl(false, true);
            }
        }
    }

    private void setALUControl(int control){
        instructionEncoding.set(F0, (control & (1 << 5)) > 0);
        instructionEncoding.set(F1, (control & (1 << 4)) > 0);
        instructionEncoding.set(ENA, (control & (1 << 3)) > 0);
        instructionEncoding.set(ENB, (control & (1 << 2)) > 0);
        instructionEncoding.set(INVA, (control & (1 << 1)) > 0);
        instructionEncoding.set(INC, (control & (1)) > 0);
    }

    private void setShifterControl(boolean ssl8, boolean sra1){
        instructionEncoding.set(SSL8, ssl8);
        instructionEncoding.set(SRA1, sra1);
    }

    private void setCurrentInstruction(MALInstruction instruction){
        instructionEncoding.clear();

        if (instruction.assignment() != null){
            MALAssignment assignment = instruction.assignment();

            if (assignment.getTarget() != null){
                instructionEncoding.set(EN_H, assignment.getTarget().contains(MALRegisters.H));
                instructionEncoding.set(EN_OPC, assignment.getTarget().contains(MALRegisters.OPC));
                instructionEncoding.set(EN_TOS, assignment.getTarget().contains(MALRegisters.TOS));
                instructionEncoding.set(EN_CPP, assignment.getTarget().contains(MALRegisters.CPP));
                instructionEncoding.set(EN_LV, assignment.getTarget().contains(MALRegisters.LV));
                instructionEncoding.set(EN_SP, assignment.getTarget().contains(MALRegisters.SP));
                instructionEncoding.set(EN_PC, assignment.getTarget().contains(MALRegisters.PC));
                instructionEncoding.set(EN_MDR, assignment.getTarget().contains(MALRegisters.MDR));
                instructionEncoding.set(EN_MAR, assignment.getTarget().contains(MALRegisters.MAR));
            }

            if (assignment.getOperation() != null){
                setAssignment(assignment);
            }



            int bus = assignment.getOperand() == null ? DEFAULT_REGISTER :
                    assignment.getOperand()
                    .stream()
                    .filter(reg -> !MALRegisters.H.equals(reg))
                    .findAny()
                    .map(REGISTER_ADDRESS::get)
                    .orElse(DEFAULT_REGISTER);

            setBus(bus);
        }

        if (instruction.ioStatement() != null){
            switch (instruction.ioStatement()){
                case READ -> instructionEncoding.set(READ);
                case WRITE -> instructionEncoding.set(WRITE);
            }
        }

        if (instruction.fetch())
            instructionEncoding.set(FETCH);

        if (instruction.controlStatement() != null){
            setControl(instruction.controlStatement());
        }
    }

    private void setNextAddress(int address){
        instructionEncoding.set(0, (address & (1 << 8)) > 0);
        instructionEncoding.set(1, (address & (1 << 7)) > 0);
        instructionEncoding.set(2, (address & (1 << 6)) > 0);
        instructionEncoding.set(3, (address & (1 << 5)) > 0);
        instructionEncoding.set(4, (address & (1 << 4)) > 0);
        instructionEncoding.set(5, (address & (1 << 3)) > 0);
        instructionEncoding.set(6, (address & (1 << 2)) > 0);
        instructionEncoding.set(7, (address & (1 << 1)) > 0);
        instructionEncoding.set(8, (address & (1)) > 0);
    }

    private void setBus(int bus){
        instructionEncoding.set(32, (bus & (1 << 3)) > 0);
        instructionEncoding.set(33, (bus & (1 << 2)) > 0);
        instructionEncoding.set(34, (bus & (1 << 1)) > 0);
        instructionEncoding.set(35, (bus & (1)) > 0);
    }

    private void writeInstruction(int address){
        int csAddress = address * 36;
        for (int i = 0; i < 36; i++){
            controlStore.set(csAddress + i, instructionEncoding.get(i));
        }
    }

}
