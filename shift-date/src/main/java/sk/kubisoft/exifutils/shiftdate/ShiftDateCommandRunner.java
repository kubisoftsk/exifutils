package sk.kubisoft.exifutils.shiftdate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;

@Singleton
public class ShiftDateCommandRunner implements CommandRunner {

    private static final Option DURATION = Option.builder()
            .option("d")
            .longOpt("duration")
            .desc("Duration to shift date and time by. Can be negative with minus sign. Format: (-)PnDTnHnMnS. For example: -PT1H30M")
            .hasArg()
            .required()
            .build();

    private static final Option RENAME = Option.builder()
            .option("r")
            .longOpt("rename")
            .desc("Rename files according to their original date and time.")
            .build();

    private final Console console;

    private final ShiftDateCommand shiftDateCommand;

    @Inject
    public ShiftDateCommandRunner(Console console, ShiftDateCommand shiftDateCommand) {
        this.console = console;
        this.shiftDateCommand = shiftDateCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            ShiftDateCommandInput input = parseInput(command);
            shiftDateCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            console.errorln("Error parsing command arguments", e);
            return 1;
        } catch (RuntimeException e) {
            console.errorln("Error executing " + getCommandName() + " command", e);
            return 1;
        }
    }

    private ShiftDateCommandInput parseInput(CommandLine cmd) throws ParseException {
        String[] args = cmd.getArgs();

        String durationString = cmd.getOptionValue(DURATION.getOpt());
        Duration duration;
        try {
            duration = Duration.parse(durationString);
        } catch (Exception e) {
            throw new ParseException("Invalid duration format: " + e.getMessage());
        }

        boolean rename = cmd.hasOption(RENAME.getOpt());

        return new ShiftDateCommandInput(args, duration, rename);
    }

    @Override
    public String getCommandName() {
        return "shift-date";
    }

    @Override
    public String getCommandDescription() {
        return "Shifts the created date of media files by a specified duration. This is useful when the camera date and time were not set correctly " +
                "and a whole set of photos needs to be shifted by the same amount to get the correct date and time.";
    }

    @Override
    public Options getOptions() {
        var options = new Options();
        options.addOption(DURATION);
        options.addOption(RENAME);
        return options;
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return  List.of(
                new CommandArgument.Builder("FILE|DIR")
                        .multiple()
                        .build()
        );
    }
}
