package sk.kubisoft.exifutils.info;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class InfoCommandRunner implements CommandRunner {

    public static final Option PRINT_ALL = Option.builder()
            .option("a")
            .longOpt("all")
            .desc("Print all available metadata information.")
            .build();

    private final Console console;

    InfoCommandInputParser inputParser;

    private final InfoCommand infoCommand;

    @Inject
    public InfoCommandRunner(Console console, InfoCommandInputParser inputParser, InfoCommand infoCommand) {
        this.console = console;
        this.inputParser = inputParser;
        this.infoCommand = infoCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            InfoCommandInput input = inputParser.parseInput(command);
            infoCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            console.errorln("Error parsing command arguments", e);
            return 1;
        } catch (RuntimeException e) {
            console.errorln("Error executing " + getCommandName() + " command", e);
            return 1;
        }
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
        options.addOption(PRINT_ALL);
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
