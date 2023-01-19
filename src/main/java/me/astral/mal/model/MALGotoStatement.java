package me.astral.mal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class MALGotoStatement extends MALControlStatement {
    private final String label;
}
