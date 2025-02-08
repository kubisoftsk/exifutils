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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RenameCommandRunner implements CommandRunner {

    private static final Option WRITE = Option.builder()
            .option("w")
            .longOpt("write-date")
            .desc("Write analyzed date to file metadata.")
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
            if (!Files.isReadable(sourceDir)) {
                throw new ParseException("Cannot read source directory: " + sourceArg);
            }
            sourceDirs.add(sourceDir);
        }

        boolean writeDate = cmd.hasOption(WRITE.getOpt());

        return new RenameCommandInput(sourceDirs, writeDate);
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
