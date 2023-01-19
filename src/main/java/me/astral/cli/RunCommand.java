package me.astral.cli;

import me.astral.mal.MAL;
import me.astral.mal.writer.MALWriter;
import me.astral.mic.MIC1Runner;
import picocli.CommandLine;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "run")
public class RunCommand implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "PROGRAM", description = "The compiled IJVM program to execute (.ijvm)")
    private File program;

    @CommandLine.Parameters(paramLabel = "CONTROL_STORE", description = "The compiled (or textual when --text-mal) control store to load")
    private File controlStore;

    @CommandLine.Option(names = {"--text-mal", "-m"})
    private boolean textualMAL;

    @Override
    public Integer call() throws Exception {
        byte[] programBytes = Files.readAllBytes(program.toPath());
        byte[] microCode = Files.readAllBytes(controlStore.toPath());

        if (textualMAL){
            microCode = new MALWriter(MAL.parse(new String(microCode, StandardCharsets.UTF_8))).write();
        }

        MIC1Runner.runIJVM(programBytes, microCode);
        return 0;
    }
}
