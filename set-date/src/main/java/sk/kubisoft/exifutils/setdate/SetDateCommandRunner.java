package sk.kubisoft.exifutils.setdate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetDateCommandRunner implements CommandRunner {

    private final SetDateCommand setDateCommand;

    private static final Option PATTERN = Option.builder()
            .option("p")
            .longOpt("pattern")
            .desc("Parse date and time from file name using given pattern (see java.time.format.DateTimeFormatter)")
            .hasArg()
            .build();

    private static final Option DATE_TIME = Option.builder()
            .option("d")
            .longOpt("date-time")
            .desc("Manually set local date and time for given files in ISO format (yyyy-MM-ddTHH:mm:ss)")
            .hasArg()
            .build();

    private static final Option ZONE_OFFSET = Option.builder()
            .option("z")
            .longOpt("zone-offset")
            .desc("Manually set zone offset for given files in ISO format (e.g. +02:00)")
            .hasArg()
            .build();

    @Inject
    public SetDateCommandRunner(SetDateCommand setDateCommand) {
        this.setDateCommand = setDateCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            SetDateCommandInput input = parseInput(command);
            setDateCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            System.err.println("Error parsing command arguments: " + e.getMessage());
            return 1;
        } catch (RuntimeException e) {
            System.err.println("Error executing " + getCommandName() + " command: " + e.getMessage());
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

        LocalDateTime dateTime = null;
        if (cmd.hasOption(DATE_TIME)) {
            try {
                String dateTimeStr = cmd.getOptionValue(DATE_TIME.getOpt());
                dateTime = LocalDateTime.parse(dateTimeStr);
            } catch (Exception e) {
                throw new ParseException("Invalid date-time format: " + e.getMessage());
            }
        }

        ZoneOffset zoneOffset = null;
        if (cmd.hasOption(ZONE_OFFSET)) {
            try {
                String zoneOffsetStr = cmd.getOptionValue(ZONE_OFFSET.getOpt());
                zoneOffset = ZoneOffset.of(zoneOffsetStr);
            } catch (Exception e) {
                throw new ParseException("Invalid zone offset format: " + e.getMessage());
            }
        }

        return new SetDateCommandInput(sourceFiles, patternStr, dateTime, zoneOffset);
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
        options.addOption(ZONE_OFFSET);
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
