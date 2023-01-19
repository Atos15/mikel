package me.astral.mic.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ALU {

    //BUS
    private int busA;
    private int busB;

    //Control
    private ALUControl control;

    //Output
    private int output;

    private boolean N;
    private boolean Z;

    public void clock(){
        int func = (control.f0() ? 1 : 0);
        func = (func << 1) + (control.f1() ? 1 : 0);
        func = (func << 1) + (control.enA() ? 1 : 0);
        func = (func << 1) + (control.enB() ? 1 : 0);
        func = (func << 1) + (control.invA() ? 1 : 0);
        func = (func << 1) + (control.inc() ? 1 : 0);

        output = switch (func){
            case 0b01_10_00 -> busA;
            case 0b01_01_00 -> busB;
            case 0b01_10_10 -> ~busA;
            case 0b10_11_00 -> ~busB;
            case 0b11_11_00 -> busA + busB;
            case 0b11_11_01 -> busA + busB + 1;
            case 0b11_10_01 -> busA + 1;
            case 0b11_01_01 -> busB + 1;
            case 0b11_11_11 -> busB - busA;
            case 0b11_01_11 -> busB - 1;
            case 0b11_10_11 -> -busA;
            case 0b00_11_00 -> busA & busB;
            case 0b01_11_00 -> busA | busB;
            case 0b01_00_00 -> 0;
            case 0b01_00_01 -> 1;
            case 0b01_00_10 -> -1;
            default -> 0;
        };

        Z = output == 0;
        N = output < 0;
    }
}
