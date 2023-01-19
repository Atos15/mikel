package me.astral.cli;


import picocli.CommandLine;

@CommandLine.Command(
        subcommands = {MicroAssembleCommand.class, RunCommand.class, DumpMIC1Command.class},
        name = "mikel"
)
public class MICLI {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MICLI())
                .execute(args);
        System.exit(exitCode);
    }

}
