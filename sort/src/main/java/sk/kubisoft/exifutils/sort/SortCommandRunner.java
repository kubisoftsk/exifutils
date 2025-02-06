package sk.kubisoft.exifutils.sort;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SortCommandRunner implements CommandRunner {

    private final SortCommand sortCommand;

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

    @Inject
    public SortCommandRunner(SortCommand sortCommand) {
        this.sortCommand = sortCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            SortCommandInput input = parseInput(command);
            sortCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            System.err.println("Error parsing command arguments: " + e.getMessage());
            return 1;
        } catch (RuntimeException e) {
            System.err.println("Error executing sort command: " + e.getMessage());
            return 1;
        }
    }

    private SortCommandInput parseInput(CommandLine cmd) throws ParseException {
        // Parse source directories - if none provided, use current directory
        List<Path> sourceDirs = new ArrayList<>();
        String[] args = cmd.getArgs();
        if (args.length == 0) {
            throw new ParseException("No source file / directory provided.");
        }
        for (String sourceArg : args) {
            Path sourceDir = Paths.get(sourceArg);
            if (!Files.exists(sourceDir)) {
                throw new ParseException("Source directory does not exist: " + sourceArg);
            }
            if (!Files.isDirectory(sourceDir)) {
                throw new ParseException("Source path is not a directory: " + sourceArg);
            }
            if (!Files.isReadable(sourceDir)) {
                throw new ParseException("Cannot read source directory: " + sourceArg);
            }
            sourceDirs.add(sourceDir);
        }

        // Parse destination directory (already validated as required by Options setup)
        String destPath = cmd.getOptionValue(DESTINATION.getOpt());
        Path destinationDir = Paths.get(destPath);

        boolean renameFiles = cmd.hasOption(RENAME.getOpt());

        // Check if destination exists and is a directory
        if (Files.exists(destinationDir)) {
            if (!Files.isDirectory(destinationDir)) {
                throw new ParseException("Destination path exists but is not a directory: " + destPath);
            }

            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(destinationDir)) {
                // TODO Make this check configurable via an option
                //if (dirStream.iterator().hasNext()) {
                //	throw new ParseException("Destination directory is not empty: " + destPath);
                //}

            } catch (IOException e) {
                throw new ParseException("Cannot access destination directory: " + e.getMessage());
            }
        } else {
            try {
                Files.createDirectories(destinationDir);
            } catch (IOException e) {
                throw new ParseException("Cannot create destination directory: " + e.getMessage());
            }
        }

        boolean writeDate = cmd.hasOption(WRITE.getOpt());

        return new SortCommandInput(sourceDirs, destinationDir, renameFiles, writeDate);
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
                new CommandArgument.Builder("DIR")
                        .multiple()
                        .build()
        );
    }
}
