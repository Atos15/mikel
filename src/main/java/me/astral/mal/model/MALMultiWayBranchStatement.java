package me.astral.mal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class MALMultiWayBranchStatement extends MALControlStatement {
    private final String address;
}
