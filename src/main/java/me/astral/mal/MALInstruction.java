package me.astral.mal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import me.astral.mal.model.MALAssignment;
import me.astral.mal.model.MALControlStatement;
import me.astral.mal.model.MALIOStatement;

@Builder
public record MALInstruction(String label, MALIOStatement ioStatement, boolean fetch, MALAssignment assignment,
                             MALControlStatement controlStatement, boolean nop) {

}
