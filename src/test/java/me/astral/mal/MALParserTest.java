package me.astral.mal;

import me.astral.mal.model.*;
import me.astral.mal.token.MALToken;
import me.astral.mal.token.MALTokenizer;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MALParserTest {

    @Test
    public void parseEmptyProgram(){
        String programSource = "";
        MALTokenizer tokenizer = new MALTokenizer(programSource);
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();
        assertEquals(0, program.instructions().size());
        assertEquals(0, program.labels().size());
        assertNull(program.defaultDirective());
    }

    @Test
    public void parseLabels(){
        String programSource = ".label test 0x00";
        MALTokenizer tokenizer = new MALTokenizer(programSource);
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();
        assertEquals(0, program.instructions().size());
        assertEquals(1, program.labels().size());
        assertNull(program.defaultDirective());

        MALLabelDirective label = program.labels().get(0);
        assertEquals("test", label.label());
        assertEquals("0x00", label.address());
    }

    @Test
    public void parseDefault(){
        String programSource = ".default\tgoto err1";
        MALTokenizer tokenizer = new MALTokenizer(programSource);
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();
        assertEquals(0, program.instructions().size());
        assertEquals(0, program.labels().size());
        assertNotNull(program.defaultDirective());

        MALInstruction defaultDirective = program.defaultDirective();
        assertEquals(MALGotoStatement.class, defaultDirective.controlStatement().getClass());
        MALGotoStatement gotoStatement = (MALGotoStatement) defaultDirective.controlStatement();
        assertEquals("err1", gotoStatement.getLabel());
    }

    @Test
    public void parseSimpleInstruction(){
        String programSource = "iadd2\tH = TOS\t\t\t\t// H = top of stack";
        MALTokenizer tokenizer = new MALTokenizer(programSource);
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();
        assertEquals(1, program.instructions().size());
        assertEquals(0, program.labels().size());
        assertNull(program.defaultDirective());

        MALInstruction instruction = program.instructions().get(0);
        assertEquals("iadd2", instruction.label());
        assertNotNull(instruction.assignment());
        MALAssignment assignment = instruction.assignment();
        List<MALRegisters> targets = assignment.getTarget();
        assertEquals(1, targets.size());
        MALRegisters target = targets.get(0);
        assertEquals(MALRegisters.H, target);

        assertEquals(MALOperations.IDENTITY, assignment.getOperation());
        assertEquals(MALRegisters.TOS, assignment.getOperand().get(0));
    }

    @Test
    public void parseLabelAndInstruction(){
        String programSource = ".label iadd1 0x60\niadd1\tMAR = SP = SP - 1; rd";
        MALTokenizer tokenizer = new MALTokenizer(programSource);
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();
        assertEquals("iadd1", program.labels().get(0).label());
        assertEquals("0x60", program.labels().get(0).address());

        MALAssignment assignment = program.instructions().get(0).assignment();
        List<MALRegisters> targets = assignment.getTarget();
        assertEquals(2, targets.size());
        assertEquals(MALRegisters.MAR, targets.get(0));
        assertEquals(MALRegisters.SP, targets.get(1));

        assertEquals(MALOperations.DEC, assignment.getOperation());
        assertEquals(MALIOStatement.READ, program.instructions().get(0).ioStatement());
    }

    @Test
    public void parseIJVMMicrocode(){
        String programSource = "// note that this is nearly identical to the example\n" +
                "// given in Tanenbaum.  Note:\n" +
                "// \n" +
                "// 1) SlashSlash-style (\"//\") comment characters have been added.\n" +
                "//\n" +
                "// 2) \"nop\" has been added as a pseudo-instruction to indicate that\n" +
                "//    nothing should be done except goto the next instruction.  It \n" +
                "//    is a do-nothing sub-instruction that allows us to have MAL\n" +
                "//    statements without a label.\n" +
                "//\n" +
                "// 3) instructions are \"anchored\" to locations in the control\n" +
                "//    store as defined below with the \".label\" pseudo-instruction\n" +
                "//\n" +
                "// 4) a default instruction may be specified using the \".default\"\n" +
                "//    pseudo-instruction.  This instruction is placed in all \n" +
                "//    unused locations of the control store by the mic1 MAL assembler.\n" +
                "//\n" +
                "\n" +
                "// labeled statements are \"anchored\" at the specified control store address\n" +
                ".label\tnop1\t\t0x00\n" +
                ".label\tbipush1\t\t0x10\n" +
                ".label\tldc_w1\t\t0x13\n" +
                ".label\tiload1\t\t0x15\n" +
                ".label\twide_iload1\t0x115\n" +
                ".label\tistore1\t\t0x36\n" +
                ".label\twide_istore1\t0x136\n" +
                ".label\tpop1\t\t0x57\n" +
                ".label\tdup1\t\t0x59\n" +
                ".label\tswap1\t\t0x5F\n" +
                ".label\tiadd1\t\t0x60\n" +
                ".label\tisub1\t\t0x64\n" +
                ".label\tiand1\t\t0x7E\n" +
                ".label\tiinc1\t\t0x84\n" +
                ".label\tifeq1\t\t0x99\n" +
                ".label\tiflt1\t\t0x9B\n" +
                ".label\tif_icmpeq1\t0x9F\n" +
                ".label\tgoto1\t\t0xA7\n" +
                ".label\tireturn1\t0xAC\n" +
                ".label\tior1\t\t0xB0\n" +
                ".label\tinvokevirtual1\t0xB6\n" +
                ".label\twide1\t\t0xC4\n" +
                ".label\thalt1\t\t0xFF\n" +
                ".label\terr1\t\t0xFE\n" +
                ".label\tout1\t\t0xFD\n" +
                ".label\tin1\t\t0xFC\n" +
                "\n" +
                "// default instruction to place in any unused addresses of the control store\n" +
                ".default\tgoto err1\n" +
                "\n" +
                "Main1\tPC = PC + 1; fetch; goto (MBR)\t// MBR holds opcode; get next byte; dispatch\n" +
                "\n" +
                "nop1\tgoto Main1\t\t\t// Do nothing\n" +
                "\n" +
                "iadd1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "iadd2\tH = TOS\t\t\t\t// H = top of stack\n" +
                "iadd3\tMDR = TOS = MDR + H; wr; goto Main1\t// Add top two words; write to top of stack\n" +
                "\n" +
                "isub1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "isub2\tH = TOS\t\t\t\t// H = top of stack\n" +
                "isub3\tMDR = TOS = MDR - H; wr; goto Main1\t// Do subtraction; write to top of stack\n" +
                "\n" +
                "iand1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "iand2\tH = TOS\t\t\t\t// H = top of stack\n" +
                "iand3\tMDR = TOS = MDR AND H; wr; goto Main1\t// Do AND; write to new top of stack\n" +
                "\n" +
                "ior1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "ior2\tH = TOS\t\t\t\t// H = top of stack\n" +
                "ior3\tMDR = TOS = MDR OR H; wr; goto Main1\t// Do OR; write to new top of stack\n" +
                "\n" +
                "dup1\tMAR = SP = SP + 1\t\t// Increment SP and copy to MAR\n" +
                "dup2\tMDR = TOS; wr; goto Main1\t// Write new stack word\n" +
                "\n" +
                "pop1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "pop2\t\t\t\t\t// Wait for new TOS to be read from memory\n" +
                "pop3\tTOS = MDR; goto Main1\t\t// Copy new word to TOS\n" +
                "\n" +
                "swap1\tMAR = SP - 1; rd\t\t// Set MAR to SP - 1; read 2nd word from stack\n" +
                "swap2\tMAR = SP\t\t\t// Set MAR to top word\n" +
                "swap3\tH = MDR; wr\t\t\t// Save TOS in H; write 2nd word to top of stack\n" +
                "swap4\tMDR = TOS\t\t\t// Copy old TOS to MDR\n" +
                "swap5\tMAR = SP - 1; wr\t\t// Set MAR to SP - 1; write as 2nd word on stack\n" +
                "swap6\tTOS = H; goto Main1\t\t// Update TOS\n" +
                "\n" +
                "bipush1\tSP = MAR = SP + 1\t\t// MBR = the byte to push onto stack\n" +
                "bipush2\tPC = PC + 1; fetch\t\t// Increment PC, fetch next opcode\n" +
                "bipush3\tMDR = TOS = MBR; wr; goto Main1\t// Sign-extend constant and push on stack\n" +
                "\n" +
                "\n" +
                "iload1\tH = LV\t\t\t\t// MBR contains index; copy LV to H\n" +
                "iload2\tMAR = MBRU + H; rd\t\t// MAR = address of local variable to push\n" +
                "iload3\tMAR = SP = SP + 1\t\t// SP points to new top of stack; prepare write\n" +
                "iload4\tPC = PC + 1; fetch; wr\t\t// Inc PC; get next opcode; write top of stack\n" +
                "iload5\tTOS = MDR; goto Main1\t\t// Update TOS\n" +
                "\n" +
                "istore1\tH = LV\t\t\t\t// MBR contains index; Copy LV to H\n" +
                "istore2\tMAR = MBRU + H\t\t\t// MAR = address of local variable to store into\n" +
                "istore3\tMDR = TOS; wr\t\t\t// Copy TOS to MDR; write word\n" +
                "istore4\tSP = MAR = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "istore5\tPC = PC + 1; fetch\t\t// Increment PC; fetch next opcode\n" +
                "istore6\tTOS = MDR; goto Main1\t\t// Update TOS\n" +
                "wide1\tPC = PC + 1; fetch; goto (MBR OR 0x100)\t// Multiway branch with high bit set\n" +
                "\n" +
                "wide_iload1\tPC = PC + 1; fetch\t// MBR contains 1st index byte; fetch 2nd\n" +
                "wide_iload2\tH = MBRU << 8\t\t// H = 1st index byte shifted left 8 bits\n" +
                "wide_iload3\tH = MBRU OR H\t\t// H = 16-bit index of local variable\n" +
                "wide_iload4\tMAR = LV + H; rd; goto iload3\t// MAR = address of local variable to push\n" +
                "\n" +
                "wide_istore1\tPC = PC + 1; fetch\t// MBR contains 1st index byte; fetch 2nd\n" +
                "wide_istore2\tH = MBRU << 8\t\t// H = 1st index byte shifted left 8 bits\n" +
                "wide_istore3\tH = MBRU OR H\t\t// H = 16-bit index of local variable\n" +
                "wide_istore4\tMAR = LV + H; goto istore3\t// MAR = address of local variable to store into\n" +
                "\n" +
                "ldc_w1\tPC = PC + 1; fetch\t\t// MBR contains 1st index byte; fetch 2nd\n" +
                "ldc_w2\tH = MBRU << 8\t\t\t// H = 1st index byte << 8\n" +
                "ldc_w3\tH = MBRU OR H\t\t\t// H = 16-bit index into constant pool\n" +
                "ldc_w4\tMAR = H + CPP; rd; goto iload3\t// MAR = address of constant in pool\n" +
                "\n" +
                "iinc1\tH = LV\t\t\t\t// MBR contains index; Copy LV to H\n" +
                "iinc2\tMAR = MBRU + H; rd\t\t// Copy LV + index to MAR; Read variable\n" +
                "iinc3\tPC = PC + 1; fetch\t\t// Fetch constant\n" +
                "iinc4\tH = MDR\t\t\t\t// Copy variable to H\n" +
                "iinc5\tPC = PC + 1; fetch\t\t// Fetch next opcode\n" +
                "iinc6\tMDR = MBR + H; wr; goto Main1\t// Put sum in MDR; update variable\n" +
                "\n" +
                "goto1\tOPC = PC - 1\t\t\t// Save address of opcode.\n" +
                "goto2\tPC = PC + 1; fetch\t\t// MBR = 1st byte of offset; fetch 2nd byte\n" +
                "goto3\tH = MBR << 8\t\t\t// Shift and save signed first byte in H\n" +
                "goto4\tH = MBRU OR H\t\t\t// H = 16-bit branch offset\n" +
                "goto5\tPC = OPC + H; fetch\t\t// Add offset to OPC\n" +
                "goto6\tgoto Main1\t\t\t// Wait for fetch of next opcode\n" +
                "\n" +
                "iflt1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word on stack\n" +
                "iflt2\tOPC = TOS\t\t\t// Save TOS in OPC temporarily\n" +
                "iflt3\tTOS = MDR\t\t\t// Put new top of stack in TOS\n" +
                "iflt4\tN = OPC; if (N) goto T; else goto F\t// Branch on N bit\n" +
                "\n" +
                "ifeq1\tMAR = SP = SP - 1; rd\t\t// Read in next-to-top word of stack\n" +
                "ifeq2\tOPC = TOS\t\t\t// Save TOS in OPC temporarily\n" +
                "ifeq3\tTOS = MDR\t\t\t// Put new top of stack in TOS\n" +
                "ifeq4\tZ = OPC; if (Z) goto T; else goto F\t// Branch on Z bit\n" +
                "\n" +
                "if_icmpeq1\tMAR = SP = SP - 1; rd\t// Read in next-to-top word of stack\n" +
                "if_icmpeq2\tMAR = SP = SP - 1\t// Set MAR to read in new top-of-stack\n" +
                "if_icmpeq3\tH = MDR; rd\t\t// Copy second stack word to H\n" +
                "if_icmpeq4\tOPC = TOS\t\t// Save TOS in OPC temporarily\n" +
                "if_icmpeq5\tTOS = MDR\t\t// Put new top of stack in TOS\n" +
                "if_icmpeq6\tZ = OPC - H; if (Z) goto T; else goto F\t// If top 2 words are equal, goto T, else goto F\n" +
                "\n" +
                "T\tOPC = PC - 1; fetch; goto goto2\t// Same as goto1; needed for target address\n" +
                "\n" +
                "F\tPC = PC + 1\t\t\t// Skip first offset byte\n" +
                "F2\tPC = PC + 1; fetch\t\t// PC now points to next opcode\n" +
                "F3\tgoto Main1\t\t\t// Wait for fetch of opcode\n" +
                "\n" +
                "invokevirtual1\tPC = PC + 1; fetch\t// MBR = index byte 1; inc. PC, get 2nd byte\n" +
                "invokevirtual2\tH = MBRU << 8\t\t// Shift and save first byte in H\n" +
                "invokevirtual3\tH = MBRU OR H\t\t// H = offset of method pointer from CPP\n" +
                "invokevirtual4\tMAR = CPP + H; rd\t// Get pointer to method from CPP area\n" +
                "invokevirtual5\tOPC = PC + 1\t\t// Save Return PC in OPC temporarily\n" +
                "invokevirtual6\tPC = MDR; fetch\t\t// PC points to new method; get param count\n" +
                "invokevirtual7\tPC = PC + 1; fetch\t// Fetch 2nd byte of parameter count\n" +
                "invokevirtual8\tH = MBRU << 8\t\t// Shift and save first byte in H\n" +
                "invokevirtual9\tH = MBRU OR H\t\t// H = number of parameters\n" +
                "invokevirtual10\tPC = PC + 1; fetch\t// Fetch first byte of # locals\n" +
                "invokevirtual11\tTOS = SP - H\t\t// TOS = address of OBJREF - 1\n" +
                "invokevirtual12\tTOS = MAR = TOS + 1\t// TOS = address of OBJREF (new LV)\n" +
                "invokevirtual13\tPC = PC + 1; fetch\t// Fetch second byte of # locals\n" +
                "invokevirtual14\tH = MBRU << 8\t\t// Shift and save first byte in H\n" +
                "invokevirtual15\tH = MBRU OR H\t\t// H = # locals\n" +
                "invokevirtual16\tMDR = SP + H + 1; wr\t// Overwrite OBJREF with link pointer\n" +
                "invokevirtual17\tMAR = SP = MDR;\t\t// Set SP, MAR to location to hold old PC\n" +
                "invokevirtual18\tMDR = OPC; wr\t\t// Save old PC above the local variables\n" +
                "invokevirtual19\tMAR = SP = SP + 1\t// SP points to location to hold old LV\n" +
                "invokevirtual20\tMDR = LV; wr\t\t// Save old LV above saved PC\n" +
                "invokevirtual21\tPC = PC + 1; fetch\t// Fetch first opcode of new method.\n" +
                "invokevirtual22\tLV = TOS; goto Main1\t// Set LV to point to LV Frame\n" +
                "\n" +
                "ireturn1\tMAR = SP = LV; rd\t// Reset SP, MAR to get link pointer\n" +
                "ireturn2\t\t\t\t// Wait for read\n" +
                "ireturn3\tLV = MAR = MDR; rd\t// Set LV to link ptr; get old PC\n" +
                "ireturn4\tMAR = LV + 1\t\t// Set MAR to read old LV\n" +
                "ireturn5\tPC = MDR; rd; fetch\t// Restore PC; fetch next opcode\n" +
                "ireturn6\tMAR = SP\t\t// Set MAR to write TOS\n" +
                "ireturn7\tLV = MDR\t\t// Restore LV\n" +
                "ireturn8\tMDR = TOS; wr; goto Main1\t// Save return value on original top of stack\n" +
                "\n" +
                "halt1\tgoto halt1\n" +
                "\n" +
                "err1\tOPC=H=-1\n" +
                "        OPC=H+OPC\n" +
                "        MAR=H+OPC\t\t\t// compute IO address\n" +
                "\tOPC=H=1\t\t\t\t// 1\n" +
                "\tOPC=H=H+OPC\t\t\t// 10\n" +
                "\tOPC=H=H+OPC\t\t\t// 100\n" +
                "\tOPC=H=H+OPC\t\t\t// 1000\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 10001\n" +
                "\tOPC=H=H+OPC\t\t\t// 100010\n" +
                "\tMDR=H+OPC+1;wr\t\t\t// 1000101 'E'\n" +
                "\tOPC=H=1\t\t\t\t// 1\n" +
                "\tOPC=H=H+OPC\t\t\t// 10\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 101\n" +
                "\tOPC=H=H+OPC\t\t\t// 1010\n" +
                "\tOPC=H=H+OPC\t\t\t// 10100\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 101001\n" +
                "\tMDR=H+OPC;wr\t\t\t// 1010010 'R'\n" +
                "        nop\n" +
                "\tMDR=H+OPC;wr\t\t\t// 1010010 'R'\n" +
                "\tOPC=H=1\t\t\t\t// 1\n" +
                "\tOPC=H=H+OPC\t\t\t// 10\n" +
                "\tOPC=H=H+OPC\t\t\t// 100\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 1001\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 10011\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 100111\n" +
                "\tMDR=H+OPC+1;wr\t\t\t// 1001111 'O'\n" +
                "\tOPC=H=1\t\t\t\t// 1\n" +
                "\tOPC=H=H+OPC\t\t\t// 10\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 101\n" +
                "\tOPC=H=H+OPC\t\t\t// 1010\n" +
                "\tOPC=H=H+OPC\t\t\t// 10100\n" +
                "\tOPC=H=H+OPC+1\t\t\t// 101001\n" +
                "\tMDR=H+OPC;wr\t\t\t// 1010010 'R'\n" +
                "\tgoto halt1\t\t\n" +
                "\n" +
                "out1\tOPC=H=-1\n" +
                "        OPC=H+OPC\n" +
                "        MAR=H+OPC\t\t\t// compute OUT address\n" +
                "\tMDR=TOS; wr\t\t\t// write to output\n" +
                "\tnop\n" +
                "\tMAR=SP=SP-1; rd                 // decrement stack pointer\n" +
                "\tnop\n" +
                "\tTOS=MDR; goto Main1\n" +
                "\n" +
                "in1\tOPC=H=-1\n" +
                "        OPC=H+OPC\n" +
                "        MAR=H+OPC;rd\t\t\t// compute IN address ; read from input\n" +
                "\tMAR=SP=SP+1\t\t\t// increment SP; wait for read\n" +
                "\tTOS=MDR;wr ; goto Main1\t\t// Write ";

        MALTokenizer tokenizer = new MALTokenizer(programSource);
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();
    }


    @Test
    public void parseSimpleOperation(){
        MALTokenizer tokenizer = new MALTokenizer("SP=SP+1");
        MALParser parser = new MALParser(tokenizer.tokenize());
        MALProgram program = parser.parse();

        MALInstruction instruction = program.instructions().get(0);
        MALAssignment assignment = instruction.assignment();
        assertEquals(1, assignment.getOperand().size());
    }

    @Test
    public void parseAddOperation(){
        MALTokenizer tokenizer = new MALTokenizer("iadd3\tMDR = TOS = MDR + H; wr; goto Main1");
        List<MALToken> tokens = tokenizer.tokenize();
        Iterator<MALToken> iter = tokens.iterator();
        assertEquals("iadd3", iter.next().value());
        assertEquals("MDR", iter.next().value());
        assertEquals("=", iter.next().value());
        assertEquals("TOS", iter.next().value());
        assertEquals("=", iter.next().value());
        assertEquals("MDR", iter.next().value());
        assertEquals("+", iter.next().value());
        assertEquals("H", iter.next().value());
        assertEquals(";", iter.next().value());
        assertEquals("wr", iter.next().value());
        assertEquals(";", iter.next().value());
        assertEquals("goto", iter.next().value());
        assertEquals("Main1", iter.next().value());
        MALProgram program = new MALParser(tokens).parse();
        MALAssignment assignment = program.instructions().get(0).assignment();
        assertEquals(MALOperations.ADD, assignment.getOperation());
        assertEquals(MALRegisters.MDR, assignment.getOperand().get(0));
        assertEquals(MALRegisters.H, assignment.getOperand().get(1));
    }

    @Test
    public void parseSDUPOperation(){
        MALTokenizer tokenizer = new MALTokenizer(
                """
                        sdup1   MAR=SP-1; rd
                        sdup2   H=TOS
                        sdup3   H=MDR-H; if (Z) goto sdup_T; else goto Main1
                                                
                        sdup_T  SP=SP-1; goto Main1
                        """
        );
        List<MALToken> tokens = tokenizer.tokenize();
        Iterator<MALToken> iter = tokens.iterator();
        assertEquals("sdup1", iter.next().value());
        assertEquals("MAR", iter.next().value());
        assertEquals("=", iter.next().value());
        assertEquals("SP", iter.next().value());
        assertEquals("-", iter.next().value());
        assertEquals("1", iter.next().value());
        assertEquals(";", iter.next().value());
        assertEquals("rd", iter.next().value());
        assertEquals("\n", iter.next().value());

        assertEquals("sdup2", iter.next().value());
        assertEquals("H", iter.next().value());
        assertEquals("=", iter.next().value());
        assertEquals("TOS", iter.next().value());
        assertEquals("\n", iter.next().value());

        assertEquals("sdup3", iter.next().value());
        assertEquals("H", iter.next().value());
        assertEquals("=", iter.next().value());
        assertEquals("MDR", iter.next().value());
        assertEquals("-", iter.next().value());
        assertEquals("H", iter.next().value());
        assertEquals(";", iter.next().value());
        assertEquals("if", iter.next().value());
        assertEquals("(", iter.next().value());
        assertEquals("Z", iter.next().value());
        assertEquals(")", iter.next().value());
        assertEquals("goto", iter.next().value());
        assertEquals("sdup_T", iter.next().value());
        assertEquals(";", iter.next().value());
        assertEquals("else", iter.next().value());
        assertEquals("goto", iter.next().value());
        assertEquals("Main1", iter.next().value());
        assertEquals("\n", iter.next().value());
        assertEquals("sdup_T", iter.next().value());
        assertEquals("SP", iter.next().value());
        assertEquals("=", iter.next().value());
        assertEquals("SP", iter.next().value());
        assertEquals("-", iter.next().value());
        assertEquals("1", iter.next().value());
        assertEquals(";", iter.next().value());
        assertEquals("goto", iter.next().value());
        assertEquals("Main1", iter.next().value());
        MALProgram program = new MALParser(tokens).parse();
        MALInstruction instruction = program.instructions().get(0);

        assertNull(instruction.controlStatement());
    }



}