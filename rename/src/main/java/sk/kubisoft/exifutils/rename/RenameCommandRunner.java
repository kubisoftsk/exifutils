package sk.kubisoft.exifutils.rename;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RenameCommandRunner implements CommandRunner {

    private final RenameCommand renameCommand;

    private static final Option DRY_RUN = Option.builder()
            .option("n")
            .longOpt("dry-run")
            .desc("Show what would be done, without making any changes.")
            .build();

    @Inject
    public RenameCommandRunner(RenameCommand renameCommand) {
        this.renameCommand = renameCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            RenameCommandInput input = parseInput(command);
            renameCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            System.err.println("Error parsing command arguments: " + e.getMessage());
            return 1;
        } catch (RuntimeException e) {
            System.err.println("Error executing " + getCommandName() + " command: " + e.getMessage());
            return 1;
        }
    }

    private RenameCommandInput parseInput(CommandLine cmd) throws ParseException {
        // Parse source directories - if none provided, use current directory
        List<Path> sourceDirs = new ArrayList<>();
        String[] args = cmd.getArgs();
        if (args.length == 0) {
            sourceDirs.add(Paths.get("."));
        } else {
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
        }

        boolean dryRun = cmd.hasOption(DRY_RUN.getOpt());

        return new RenameCommandInput(sourceDirs, dryRun);
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
        options.addOption(DRY_RUN);
        return options;
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return  List.of(
                new CommandArgument.Builder("DIR")
                        .optional()
                        .multiple()
                        .build()
        );
    }
}
