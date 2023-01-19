package me.astral.mic.model;

public record BusCControl(
        boolean enableH,
        boolean enableOPC,
        boolean enableTOS,
        boolean enableCPP,
        boolean enableLV,
        boolean enableSP,
        boolean enablePC,
        boolean enableMDR,
        boolean enableMAR
) {
}
