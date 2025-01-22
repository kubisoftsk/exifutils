package sk.kubisoft.exifutils.setdate;

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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetDateCommandRunner implements CommandRunner {

    private final SetDateCommand setDateCommand;

    private static final Option DATE_TIME = Option.builder()
            .option("d")
            .longOpt("date-time")
            .desc("Set local date and time in ISO format (yyyy-MM-ddTHH:mm:ss)")
            .hasArg()
            .required()
            .build();

    private static final Option ZONE_OFFSET = Option.builder()
            .option("z")
            .longOpt("zone-offset")
            .desc("Set zone offset in ISO format (e.g. +02:00)")
            .hasArg()
            .required()
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
                throw new ParseException("Source file does not exist: " + sourceArg);
            }
            if (!Files.isRegularFile(sourceFile)) {
                throw new ParseException("Source file is not a regular file: " + sourceArg);
            }
            if (!Files.isReadable(sourceFile)) {
                throw new ParseException("Cannot read source file: " + sourceArg);
            }
            sourceFiles.add(sourceFile);
        }
        String dateTimeStr = cmd.getOptionValue(DATE_TIME.getOpt());
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            throw new ParseException("Invalid date-time format: " + e.getMessage());
        }
        String zoneOffsetStr = cmd.getOptionValue(ZONE_OFFSET.getOpt());
        ZoneOffset zoneOffset;
        try {
            zoneOffset = ZoneOffset.of(zoneOffsetStr);
        } catch (Exception e) {
            throw new ParseException("Invalid zone offset format: " + e.getMessage());
        }

        return new SetDateCommandInput(sourceFiles, dateTime, zoneOffset);
    }

    @Override
    public String getCommandName() {
        return "set-date";
    }

    @Override
    public String getCommandDescription() {
        return "Sets created date of media files";
    }

    @Override
    public Options getOptions() {
        var options = new Options();
        options.addOption(DATE_TIME);
        options.addOption(ZONE_OFFSET);
        return options;
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return  List.of(
                new CommandArgument.Builder("FILE")
                        .multiple()
                        .build()
        );
    }
}
