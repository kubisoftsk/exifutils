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

    private static final Option OUTPUT_DIR = Option.builder()
            .option("o").hasArg()
            .longOpt("outputDir").hasArg().argName("DIR")
            .required()
            .desc("Root output directory for the sorted files.")
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

    private static final Option PATTERN = Option.builder()
            .option("p").hasArg()
            .longOpt("pattern").hasArg().argName("PATTERN")
            .desc("Custom folder pattern using ${date,FORMAT} syntax (e.g., ${date,yyyy}/${date,MM}/${date,dd}).")
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

        // Parse output directory (already validated as required by Options setup)
        String outputDirStr = cmd.getOptionValue(OUTPUT_DIR.getOpt());
        Path outputDir = Paths.get(outputDirStr);

        boolean renameFiles = cmd.hasOption(RENAME.getOpt());

        // Check if output directory exists and is a directory
        if (Files.exists(outputDir)) {
            if (!Files.isDirectory(outputDir)) {
                throw new ParseException("Output path exists but is not a directory: " + outputDirStr);
            }
        } else {
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                throw new ParseException("Cannot create output directory: " + e.getMessage());
            }
        }

        boolean writeDate = cmd.hasOption(WRITE.getOpt());

        String sortPattern = cmd.getOptionValue(PATTERN.getOpt());

        return new SortCommandInput(args, outputDir, renameFiles, writeDate, sortPattern);
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
        options.addOption(OUTPUT_DIR);
        options.addOption(RENAME);
        options.addOption(WRITE);
        options.addOption(PATTERN);
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
