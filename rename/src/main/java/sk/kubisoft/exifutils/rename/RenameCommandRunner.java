package sk.kubisoft.exifutils.rename;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;

@Singleton
public class RenameCommandRunner implements CommandRunner {

    private static final Option OUTPUT_DIR = Option.builder()
            .option("o")
            .longOpt("output-dir")
            .desc("Output directory for renamed files with flat structure (source subdirectories are left out). If not set, files are renamed in place.")
            .hasArg()
            .build();

    private static final Option WRITE = Option.builder()
            .option("w")
            .longOpt("write-date")
            .desc("Write analyzed date to file metadata.")
            .build();

    private static final Option ZONE_ID = Option.builder()
            .option("z")
            .longOpt("zone-id")
            .desc("Use given time-zone ID for given files, such as Europe/Paris. If not set, default time-zone from config is used. Works only with -w option.")
            .hasArg()
            .build();

    private final Console console;

    private final RenameCommand renameCommand;

    @Inject
    public RenameCommandRunner(Console console, RenameCommand renameCommand) {
        this.console = console;
        this.renameCommand = renameCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            RenameCommandInput input = parseInput(command);
            renameCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            console.errorln("Error parsing command arguments", e);
            return 1;
        } catch (RuntimeException e) {
            console.errorln("Error executing " + getCommandName() + " command", e);
            return 1;
        }
    }

    private RenameCommandInput parseInput(CommandLine cmd) throws ParseException {
        String[] args = cmd.getArgs();

        boolean writeDate = cmd.hasOption(WRITE.getOpt());

        ZoneId zoneId = null;
        if (cmd.hasOption(ZONE_ID)) {
            try {
                String zoneIdStr = cmd.getOptionValue(ZONE_ID.getOpt());
                zoneId = ZoneId.of(zoneIdStr);
            } catch (Exception e) {
                throw new ParseException("Invalid time-zone ID: " + e.getMessage());
            }
        }

        Path outputDir = null;
        if (cmd.hasOption(OUTPUT_DIR)) {
            String outputDirStr = cmd.getOptionValue(OUTPUT_DIR.getOpt());
            outputDir = Path.of(outputDirStr);
            if (!outputDir.toFile().exists()) {
                throw new ParseException("Output path does not exist: " + outputDirStr);
            }
            if (!outputDir.toFile().isDirectory()) {
                throw new ParseException("Output path is not a directory: " + outputDirStr);
            }
        }

        return new RenameCommandInput(args, outputDir, writeDate, zoneId);
    }

    @Override
    public String getCommandName() {
        return "rename";
    }

    @Override
    public String getCommandDescription() {
        return "Renames media files based on EXIF date";
    }

    @Override
    public Options getOptions() {
        var options = new Options();
        options.addOption(WRITE);
        options.addOption(ZONE_ID);
        options.addOption(OUTPUT_DIR);
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
