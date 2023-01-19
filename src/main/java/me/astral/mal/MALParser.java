package me.astral.mal;

import me.astral.mal.model.*;
import me.astral.mal.token.MALToken;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MALParser {

    private int index = 0;
    private final List<MALToken> source;

    public MALParser(List<MALToken> source){
        this.source = source;
    }

    private MALToken current(){
        return source.get(index);
    }
    private MALToken advance() { return source.get(index++); }

    private void assertAndAdvance(String value){
        MALToken curr = advance();
        if (!value.equals(curr.value()))
            throw new IllegalStateException("Expected " + value + " but found " + curr.value());
    }

    private void doneOrAssertAndAdvance(String value){
        if (done())
            return;
        assertAndAdvance(value);
    }

    private MALToken next() {
        return source.get(++index);
    }

    private MALToken peekNext(){
        return source.get(index + 1);
    }

    private boolean done(){
        return index >= source.size();
    }

    private boolean hasNext(){
        return index < source.size() - 1;
    }

    private void nextIndex(){
        index++;
    }

    private void nextIndex(int skip){
        index += skip;
    }

    public MALProgram parse(){
        try{
            MALProgram.MALProgramBuilder builder = MALProgram.builder();

            while (!done()){
                MALToken current = current();
                String currentValue = current.value();
                if (current.type() == MALToken.Type.BEGIN_DIRECTIVE) {
                    if (".label".equals(currentValue)) parseLabel(builder);
                    if (".default".equals(currentValue)) parseDefault(builder);
                } else {
                    builder.instruction(parseInstruction());
                }
            }

            return builder.build();
        }catch (Exception e){
            throw new RuntimeException("An error occurred while parsing at token " + this.index, e);
        }
    }

    private final static Set<String> KEYWORDS = new HashSet<>();

    static {
        KEYWORDS.addAll(Arrays.asList(
                "MAR",
                "MDR",
                "PC",
                "MBR",
                "MBRU",
                "SP",
                "LV",
                "CPP",
                "TOS",
                "OPC",
                "H",
                "Z",
                "N",
                "rd",
                "wr",
                "fetch",
                "if",
                "else",
                "goto",
                "nop",
                "AND",
                "OR",
                "NOT"
        ));
    }

    private boolean isKeyword(MALToken token){
        return KEYWORDS.contains(token.value());
    }

    private static final Set<String> IO_KEYWORDS = Set.of("rd", "wr", "fetch");

    private MALInstruction parseInstruction(){
        MALInstruction.MALInstructionBuilder instructionBuilder = MALInstruction.builder();
        if (!isKeyword(current())){
            instructionBuilder.label(advance().value());
        }

        while(!done() && !MALToken.Type.EOL.equals(current().type())){
            if (current().value().equals("if")) {
                parseIfStatement(instructionBuilder);
            }else if (current().value().equals("goto")){
                if (peekNext().value().equals("(")){
                    parseMultiwayBranchStatement(instructionBuilder);
                }else{
                    parseGotoStatement(instructionBuilder);
                }
            }else if (IO_KEYWORDS.contains(current().value())){
                parseIOStatement(instructionBuilder);
            }else if (current().value().equals("nop")){
                parseNOPStatement(instructionBuilder);
            }else {
                parseAssignment(instructionBuilder);
            }

            if (!done() && ";".equals(current().value()))
                advance();
        }

        nextIndex(); //Skips EOL
        return instructionBuilder.build();
    }

    private void parseMultiwayBranchStatement(MALInstruction.MALInstructionBuilder instruction){
        String address = null;
        assertAndAdvance("goto");
        assertAndAdvance("(");
        assertAndAdvance("MBR");
        if (current().value().equals("OR")){
            address = next().value();
            nextIndex();
        }
        assertAndAdvance(")");
        instruction.controlStatement(new MALMultiWayBranchStatement(
            address
        ));
    }

    private void parseIfStatement(MALInstruction.MALInstructionBuilder instruction){
        assertAndAdvance("if");
        assertAndAdvance("(");
        MALToken condition = advance();
        assertAndAdvance(")");
        assertAndAdvance("goto");
        MALToken trueLabel = advance();
        assertAndAdvance(";");
        assertAndAdvance("else");
        assertAndAdvance("goto");
        MALToken falseLabel = advance();

        instruction.controlStatement(new MALIfStatement(
                MALRegisters.valueOf(condition.value()),
                trueLabel.value(),
                falseLabel.value()
        ));
    }

    private void parseGotoStatement(MALInstruction.MALInstructionBuilder instruction){
        assertAndAdvance("goto");
        String label = advance().value();
        instruction.controlStatement(new MALGotoStatement(
                label
        ));
    }


    private void parseIOStatement(MALInstruction.MALInstructionBuilder instruction){
        String value = advance().value();

        if ("fetch".equals(value)) {
            instruction.fetch(true);
        } else {
            instruction.ioStatement(MALIOStatement.fromKeyword(value));
        }
    }

    private void parseNOPStatement(MALInstruction.MALInstructionBuilder instruction){
        nextIndex();
        instruction.nop(true);
    }

    private void parseAssignment(MALInstruction.MALInstructionBuilder instruction){
        List<MALRegisters> targets = new ArrayList<>();
        MALAssignment assignment = new MALAssignment();
        assignment.setTarget(targets);
        while(hasNext() && "=".equals(peekNext().value())){
            targets.add(MALRegisters.valueOf(advance().value()));
            nextIndex();
        }
        parseExpression(assignment);
        instruction.assignment(assignment);
    }



    private void parseExpression(MALAssignment assignment){
        parseOperation(assignment);

        if (done())
            return;

        if ("<<".equals(current().value())){
            if (!"8".equals(next().value()))
                throw new IllegalStateException("An 8 must follow <<");
            assignment.setModifier(MALExpressionModifier.LEFT_SHIFT_8);
            nextIndex();
        } else if (">>".equals(current().value())) {
            if (!"1".equals(next().value()))
                throw new IllegalStateException("A 1 must follow >>");
            assignment.setModifier(MALExpressionModifier.RIGHT_SHIFT_1);
            nextIndex();
        }
    }

    private static final Set<String> A_TERMS = Set.of("H");
    private static final Set<String> B_TERMS = Set.of("MDR", "PC", "MBR", "MBRU", "SP", "LV", "CPP", "TOS", "OPC");
    private void parseOperation(MALAssignment assignment){
        boolean aTerm = false;
        boolean bTerm = false;
        if ("NOT".equals(current().value())){
            assignment.setOperation(MALOperations.NOT);
            assignment.setOperand(
                    List.of(MALRegisters.valueOf(next().value()))
            );
            nextIndex();
        }else if ("-".equals(current().value())){

            if (peekNext().value().equals("1")){
                assignment.setOperation(MALOperations.CONSTANT_MINUS_ONE);
                nextIndex(2);
            }else{
                assignment.setOperation(MALOperations.NEGATE);
                assignment.setOperand(
                        List.of(MALRegisters.valueOf(next().value()))
                );
                nextIndex();
            }
        }else if ("0".equals(current().value())){
            assignment.setOperation(MALOperations.CONSTANT_ZERO);
            nextIndex();
        }else if ("1".equals(current().value())){
            assignment.setOperation(MALOperations.CONSTANT_ONE);
            nextIndex();
        }else{

            MALToken firstOperand = advance();
            String firstOperandValue = firstOperand.value();
            if (B_TERMS.contains(firstOperandValue))
                bTerm = true;
            else if (A_TERMS.contains(firstOperandValue))
                aTerm = true;
            else throw new IllegalStateException("Expected one of the following " + Stream.concat(
                    A_TERMS.stream(), B_TERMS.stream()
                ).collect(Collectors.joining(", ", "'", "'")) + " but found " + firstOperandValue);

            if (done()){
                assignment.setOperation(MALOperations.IDENTITY);
                assignment.setOperand(List.of(
                        MALRegisters.valueOf(firstOperand.value())
                ));
            }else{
                switch (current().value()){
                    case "AND" -> {
                        assignment.setOperation(MALOperations.AND);
                        assignment.setOperand(List.of(
                                MALRegisters.valueOf(firstOperand.value()),
                                MALRegisters.valueOf(assertValidOperand(next().value(), aTerm, bTerm))
                        ));
                        nextIndex();
                    }
                    case "OR" -> {
                        assignment.setOperation(MALOperations.OR);
                        assignment.setOperand(List.of(
                                MALRegisters.valueOf(firstOperand.value()),
                                MALRegisters.valueOf(assertValidOperand(next().value(), aTerm, bTerm))
                        ));
                        nextIndex();
                    }
                    case "+" -> {
                        MALToken secondOperand = next();

                        if ("1".equals(current().value())) {
                            assignment.setOperation(MALOperations.INC);
                            assignment.setOperand(List.of(
                                    MALRegisters.valueOf(firstOperand.value())
                            ));
                            nextIndex();
                        } else {
                            if ("+".equals(peekNext().value())) {
                                nextIndex(2);
                                if (!"1".equals(current().value()))
                                    throw new IllegalStateException("Triple sum should end with '1'");

                                assignment.setOperation(MALOperations.ADD_INC);
                                assignment.setOperand(List.of(
                                        MALRegisters.valueOf(firstOperand.value()),
                                        MALRegisters.valueOf(assertValidOperand(secondOperand.value(), aTerm, bTerm))
                                ));
                                nextIndex();
                            } else {
                                nextIndex();
                                assignment.setOperation(MALOperations.ADD);
                                assignment.setOperand(List.of(
                                        MALRegisters.valueOf(firstOperand.value()),
                                        MALRegisters.valueOf(assertValidOperand(secondOperand.value(), aTerm, bTerm))
                                ));
                            }
                        }
                    }
                    case "-" -> {
                        String secondOperand = next().value();
                        nextIndex();

                        if ("1".equals(secondOperand)){
                            assignment.setOperation(MALOperations.DEC);
                            assignment.setOperand(List.of(
                                    MALRegisters.valueOf(firstOperand.value())
                            ));
                        }else if ("H".equals(secondOperand)){
                            assignment.setOperation(MALOperations.SUB);
                            assignment.setOperand(List.of(
                                    MALRegisters.valueOf(firstOperand.value()),
                                    MALRegisters.valueOf(secondOperand)
                            ));
                        }else throw new IllegalStateException("Expected 'H' after '-' but found '" + secondOperand + "'");
                    }
                    default -> {
                        assignment.setOperation(MALOperations.IDENTITY);
                        assignment.setOperand(List.of(
                                MALRegisters.valueOf(firstOperand.value())
                        ));
                    }
                }
            }

        }
    }

    private String assertValidOperand(String value, boolean aTerm, boolean bTerm){
        if (B_TERMS.contains(value)){
            if (bTerm) throw new IllegalStateException("Found multiple BusB registers in a single operation");
            return value;
        }
        else if (A_TERMS.contains(value)) {
            if (aTerm) throw new IllegalStateException("Found multiple BusA registers in a single operation");
            return value;
        }
        else throw new IllegalStateException("Expected one of the following " + Stream.concat(
                    A_TERMS.stream(), B_TERMS.stream()
            ).collect(Collectors.joining(", ", "'", "'")) + " but found " + value);
    }

    private void parseLabel(MALProgram.MALProgramBuilder builder){
        assertAndAdvance(".label");
        MALToken name = advance();
        MALToken address = advance();
        doneOrAssertAndAdvance("\n");
        builder.label(new MALLabelDirective(name.value(), address.value()));
    }

    private void parseDefault(MALProgram.MALProgramBuilder builder){
        MALInstruction instruction = parseInstruction();
        builder.defaultDirective(instruction);
    }

}
