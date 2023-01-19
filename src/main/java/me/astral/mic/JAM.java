package me.astral.mic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JAM {
    JMPC((byte) (1 << 2)),
    JAMN((byte) (1 << 1)),
    JAMZ((byte) 1),
    NONE((byte) 0);

    private final byte value;
}
