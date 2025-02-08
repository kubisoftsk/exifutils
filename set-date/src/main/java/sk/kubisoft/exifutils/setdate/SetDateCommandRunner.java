package sk.kubisoft.exifutils.setdate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetDateCommandRunner implements CommandRunner {

    private static final Option PATTERN = Option.builder()
            .option("p")
            .longOpt("pattern")
            .desc("Parse date and time from file name using given pattern (see java.time.format.DateTimeFormatter)")
            .hasArg()
            .build();

    private static final Option DATE_TIME = Option.builder()
            .option("d")
            .longOpt("date-time")
            .desc("Manually set local date and time for given files in format 'yyyy-MM-dd HH:mm:ss XXX' - for example '2021-01-01 12:00:00 +02:00'")
            .hasArg()
            .build();

    private static final Option RENAME = Option.builder()
            .option("r")
            .longOpt("rename")
            .desc("Rename files according to their original date and time.")
            .build();

    private final Console console;

    private final SetDateCommand setDateCommand;

    @Inject
    public SetDateCommandRunner(Console console, SetDateCommand setDateCommand) {
        this.console = console;
        this.setDateCommand = setDateCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            SetDateCommandInput input = parseInput(command);
            setDateCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            console.errorln("Error parsing command arguments", e);
            return 1;
        } catch (RuntimeException e) {
            console.errorln("Error executing " + getCommandName() + " command", e);
            return 1;
        }
    }

    private SetDateCommandInput parseInput(CommandLine cmd) throws ParseException{
        List<Path> sourceFiles = new ArrayList<>();
        String[] args = cmd.getArgs();
        for (String sourceArg : args) {
            Path sourceFile = Paths.get(sourceArg);
            if (!Files.exists(sourceFile)) {
                throw new ParseException("Source file/directory does not exist: " + sourceArg);
            }
            if (!Files.isReadable(sourceFile)) {
                throw new ParseException("Cannot read source file: " + sourceArg);
            }
            sourceFiles.add(sourceFile);
        }
        String patternStr = cmd.getOptionValue(PATTERN.getOpt());
        if (StringUtils.isNotBlank(patternStr)) {
            try {
                DateTimeFormatter.ofPattern(patternStr);
            } catch (Exception e) {
                throw new ParseException("Invalid date-time pattern: " + e.getMessage());
            }
        }

        OffsetDateTime dateTime = null;
        if (cmd.hasOption(DATE_TIME)) {
            try {
                String dateTimeStr = cmd.getOptionValue(DATE_TIME.getOpt());
                dateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX"));
            } catch (Exception e) {
                throw new ParseException("Invalid date-time format: " + e.getMessage());
            }
        }

        boolean rename = cmd.hasOption(RENAME.getOpt());
        return new SetDateCommandInput(sourceFiles, patternStr, dateTime, rename);
    }

    @Override
    public String getCommandName() {
        return "set-date";
    }

    @Override
    public String getCommandDescription() {
        return "Sets created date of media files. If not specified pattern or manual date time, it will try to parse date from file name by common patterns.";
    }

    @Override
    public Options getOptions() {
        var options = new Options();
        options.addOption(PATTERN);
        options.addOption(DATE_TIME);
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
