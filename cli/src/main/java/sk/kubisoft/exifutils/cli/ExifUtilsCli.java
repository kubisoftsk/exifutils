package sk.kubisoft.exifutils.cli;

import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;

public class ExifUtilsCli {

    private final Map<String, CommandRunner> commands;
    private final Console console;

    // Common options
    private static final Option VERBOSE = Option.builder()
            .option("v")
            .longOpt("verbose")
            .desc("Print verbose output.")
            .get();

    @Inject
    public ExifUtilsCli(Map<String, CommandRunner> commands, Console console) {
        this.commands = commands;
        this.console = console;
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
        if (commandArgs.length == 0) {
            // Display help for command without parsing errors
            printCommandHelp(runner);
            System.exit(0);
        }

        CommandLineParser parser = new DefaultParser();
        var options = runner.getOptions();
        options.addOption(VERBOSE);
        try {
            CommandLine cmd = parser.parse(options, commandArgs);

            if (!validateRequiredArguments(runner, cmd)) {
                System.err.println("Missing required argument(s).");
                printCommandHelp(runner);
                System.exit(1);
            }

            if (cmd.hasOption(VERBOSE) && console instanceof sk.kubisoft.exifutils.cli.logging.SystemConsole systemConsole) {
                systemConsole.setVerbose(true);
            }

            System.exit(runner.runCommand(cmd));
        } catch (ParseException e) {
            System.err.println("Error parsing command arguments: " + e.getMessage());
            printCommandHelp(runner);
            System.exit(2);
        }
    }

    private boolean validateRequiredArguments(CommandRunner runner, CommandLine cmd) {
        var commandArguments = runner.getCommandArguments();
        var providedArgs = cmd.getArgs();

        int requiredCount = 0;
        for (var arg : commandArguments) {
            if (arg.isRequired()) {
                requiredCount++;
            }
        }

        return providedArgs.length >= requiredCount;
    }

    private void printUsage() {
        System.out.println("Usage: exifutils <command> [options...]");
        System.out.println("\nAvailable commands:");
        commands.values().forEach(cmd ->
                System.out.printf("  %-15s %s%n",
                        cmd.getCommandName(),
                        cmd.getCommandDescription()));
    }

    private void printCommandHelp(CommandRunner runner) {
        HelpFormatter formatter = HelpFormatter.builder()
                .setShowSince(false)
                .get();
        String cmdLineSyntax = "exifutils " + runner.getCommandName() + " " + OptionsFormatter.generateUsageSyntax(runner.getOptions(), runner.getCommandArguments());
        String header = runner.getCommandDescription();

        try {
            formatter.printHelp(cmdLineSyntax, header, runner.getOptions(), null, false);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
