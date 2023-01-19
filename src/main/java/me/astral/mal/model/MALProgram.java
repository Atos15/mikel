package me.astral.mal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import me.astral.mal.MALInstruction;

import java.util.List;

@Builder
public record MALProgram(MALInstruction defaultDirective, @Singular List<MALLabelDirective> labels,
                         @Singular List<MALInstruction> instructions) {

}
