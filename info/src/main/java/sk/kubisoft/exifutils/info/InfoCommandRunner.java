package sk.kubisoft.exifutils.info;

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
public class InfoCommandRunner implements CommandRunner  {

    private final InfoCommand infoCommand;

    private static final Option EXTRACT_DATE = Option.builder()
            .option("d")
            .longOpt("extract-date")
            .desc("Flag to also print extracted date information from given files.")
            .build();

    @Inject
    public InfoCommandRunner(InfoCommand infoCommand) {
        this.infoCommand = infoCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            InfoCommandInput input = parseInput(command);
            infoCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            System.err.println("Error parsing command arguments: " + e.getMessage());
            return 1;
        } catch (RuntimeException e) {
            System.err.println("Error executing " + getCommandName() + " command: " + e.getMessage());
            return 1;
        }
    }

    private InfoCommandInput parseInput(CommandLine cmd) throws ParseException {
        // Parse source directories - if none provided, use current directory
        List<Path> sourceDirs = new ArrayList<>();
        String[] args = cmd.getArgs();
        if (args.length == 0) {
            sourceDirs.add(Paths.get("."));
        } else {
            for (String sourceArg : args) {
                Path sourceDir = Paths.get(sourceArg);
                if (!Files.exists(sourceDir)) {
                    throw new ParseException("Source file / directory does not exist: " + sourceArg);
                }
                if (!Files.isReadable(sourceDir)) {
                    throw new ParseException("Cannot read source file / directory: " + sourceArg);
                }
                sourceDirs.add(sourceDir);
            }
        }

        boolean extractDate = cmd.hasOption(EXTRACT_DATE.getOpt());

        return new InfoCommandInput(sourceDirs, extractDate);
    }

    @Override
    public String getCommandName() {
        return "info";
    }

    @Override
    public String getCommandDescription() {
        return "Prints extracted metadata information for given files.";
    }

    @Override
    public Options getOptions() {
        var options = new Options();
        options.addOption(EXTRACT_DATE);
        return options;
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return  List.of(
                new CommandArgument.Builder("FILE|DIR")
                        .optional()
                        .multiple()
                        .build()
        );
    }
}
