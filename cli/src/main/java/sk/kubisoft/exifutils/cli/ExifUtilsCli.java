package sk.kubisoft.exifutils.cli;

import org.apache.commons.cli.*;
import sk.kubisoft.exifutils.cli.logging.LoggingUtils;
import sk.kubisoft.exifutils.cli.logging.SystemConsole;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;

public class ExifUtilsCli {

    private final Map<String, CommandRunner> commands;
    private final SystemConsole systemConsole;

    // Common options
    private static final Option VERBOSE = Option.builder()
            .option("v")
            .longOpt("verbose")
            .desc("Print verbose output.")
            .build();

    @Inject
    public ExifUtilsCli(Map<String, CommandRunner> commands, SystemConsole console) {
        this.commands = commands;
        this.systemConsole = console;
    }

    public void run(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String commandName = args[0];
        CommandRunner runner = commands.get(commandName);

        if (runner == null) {
            System.err.println("Unknown command: " + commandName);
            printUsage();
            System.exit(1);
        }

        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        CommandLineParser parser = new DefaultParser();
        var options = runner.getOptions();
        options.addOption(VERBOSE);
        try {
            CommandLine cmd = parser.parse(options, commandArgs);

            if (cmd.hasOption("verbose")) {
                systemConsole.setVerbose(true);
            }

            LoggingUtils.getCurrentLogFile().ifPresent(logFile -> {
                System.out.println("Started logging to file: " + logFile.toAbsolutePath());
            });

            System.exit(runner.runCommand(cmd));
        } catch (ParseException e) {
            System.err.println("Error parsing command arguments: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "exifutils " + commandName + " " + OptionsFormatter.generateUsageSyntax(options, runner.getCommandArguments()),
                    runner.getCommandDescription(),
                    options,
                    null
            );
            System.exit(1);
        }
    }

    private void printUsage() {
        System.out.println("Usage: exifutils <command> [options...]");
        System.out.println("\nAvailable commands:");
        commands.values().forEach(cmd ->
                System.out.printf("  %-15s %s%n",
                        cmd.getCommandName(),
                        cmd.getCommandDescription()));
    }

}
