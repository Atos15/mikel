package me.astral.cli;

import me.astral.mal.MAL;
import me.astral.mal.model.MALProgram;
import me.astral.mal.writer.MALWriter;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "assemble")
public class MicroAssembleCommand implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "FILE", description = "A .mal textual file")
    private File malFile;

    @CommandLine.Parameters(paramLabel = "OUTPUT", description = "The output binary control store")
    private File outputFile;

    @Override
    public Integer call() throws Exception {
        System.out.println("Assembling file " + malFile.getAbsolutePath() + "...");
        String code = Files.readString(malFile.toPath());
        MALProgram program = MAL.parse(code);
        MALWriter writer = new MALWriter(program);
        byte[] controlStore = writer.write();
        Files.write(outputFile.toPath(), controlStore);
        System.out.println("Generated mic1 control store binary at " + outputFile.getAbsolutePath());
        return 0;
    }
}
