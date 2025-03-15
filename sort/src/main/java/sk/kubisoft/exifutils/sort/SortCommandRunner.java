package sk.kubisoft.exifutils.sort;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Singleton
public class SortCommandRunner implements CommandRunner {

    private static final Option DESTINATION = Option.builder()
            .option("d").hasArg()
            .longOpt("destination").hasArg().argName("DIR")
            .required()
            .desc("Destination root directory for the sorted files.")
            .build();

    private static final Option RENAME = Option.builder()
            .option("r")
            .longOpt("rename")
            .desc("Rename files according to their original date and time.")
            .build();

    private static final Option WRITE = Option.builder()
            .option("w")
            .longOpt("write-date")
            .desc("Write analyzed date to file metadata.")
            .build();

    private final SortCommand sortCommand;

    private final Console console;

    @Inject
    public SortCommandRunner(Console console, SortCommand sortCommand) {
        this.console = console;
        this.sortCommand = sortCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            SortCommandInput input = parseInput(command);
            sortCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            console.errorln("Error parsing command arguments", e);
            return 1;
        } catch (RuntimeException e) {
            console.errorln("Error executing " + getCommandName() + " command", e);
            return 1;
        }
    }

    private SortCommandInput parseInput(CommandLine cmd) throws ParseException {
        String[] args = cmd.getArgs();

        // Parse destination directory (already validated as required by Options setup)
        String destPath = cmd.getOptionValue(DESTINATION.getOpt());
        Path destinationDir = Paths.get(destPath);

        boolean renameFiles = cmd.hasOption(RENAME.getOpt());

        // Check if destination exists and is a directory
        if (Files.exists(destinationDir)) {
            if (!Files.isDirectory(destinationDir)) {
                throw new ParseException("Destination path exists but is not a directory: " + destPath);
            }
        } else {
            try {
                Files.createDirectories(destinationDir);
            } catch (IOException e) {
                throw new ParseException("Cannot create destination directory: " + e.getMessage());
            }
        }

        boolean writeDate = cmd.hasOption(WRITE.getOpt());

        return new SortCommandInput(args, destinationDir, renameFiles, writeDate);
    }

    @Override
    public String getCommandName() {
        return "sort";
    }

    @Override
    public String getCommandDescription() {
        return "Sorts media files by date";
    }

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption(DESTINATION);
        options.addOption(RENAME);
        options.addOption(WRITE);
        return options;
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return List.of(
                new CommandArgument.Builder("FILE|DIR")
                        .multiple()
                        .build()
        );
    }
}
