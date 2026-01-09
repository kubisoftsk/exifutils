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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            .desc("Manually set local date and time for given files in format 'yyyy-MM-dd HH:mm:ss' - for example '2021-01-01 12:00:00'")
            .hasArg()
            .build();

    private static final Option ZONE_ID = Option.builder()
            .option("z")
            .longOpt("zone-id")
            .desc("Manually set time-zone ID for given files, such as Europe/Paris. If not set, default time-zone from config is used.")
            .hasArg()
            .build();

    private static final Option RENAME = Option.builder()
            .option("r")
            .longOpt("rename")
            .desc("Rename files according to their original date and time.")
            .build();

    private static final Option UNKNOWN_ONLY = Option.builder()
            .option("u")
            .longOpt("unknown")
            .desc("Set date and time only to files with unknown date and time. This option will skip files with already set date and time.")
            .build();

    private static final Option FIX_ZONE = Option.builder()
            .option("f")
            .longOpt("fix-zone")
            .desc("Fix timezone for files that have correct local date/time but wrong or missing timezone. " +
                    "Uses existing EXIF date/time but applies the timezone from --zone-id (or default config).")
            .build();

    private static final Option FORCE_FIELD = Option.builder()
            .option("F")
            .longOpt("force-field")
            .desc("Force date extraction from the specified EXIF field, bypassing device profiles. Use 'info -a' to list available fields. Works with --fix-zone and --unknown options.")
            .hasArg()
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
        String[] args = cmd.getArgs();

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
                dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                throw new ParseException("Invalid date-time format: " + e.getMessage());
            }
        }

        ZoneId zoneId = null;
        if (cmd.hasOption(ZONE_ID)) {
            try {
                String zoneIdStr = cmd.getOptionValue(ZONE_ID.getOpt());
                zoneId = ZoneId.of(zoneIdStr);
            } catch (Exception e) {
                throw new ParseException("Invalid time-zone ID: " + e.getMessage());
            }
        }

        boolean rename = cmd.hasOption(RENAME.getOpt());
        boolean unknownOnly = cmd.hasOption(UNKNOWN_ONLY.getOpt());
        boolean fixZone = cmd.hasOption(FIX_ZONE.getOpt());
        String forceField = cmd.getOptionValue(FORCE_FIELD.getOpt());

        if (fixZone) {
            if (dateTime != null) {
                throw new ParseException("Options --fix-zone and --date-time are mutually exclusive.");
            }
            if (StringUtils.isNotBlank(patternStr)) {
                throw new ParseException("Options --fix-zone and --pattern are mutually exclusive.");
            }
            if (unknownOnly) {
                throw new ParseException("Options --fix-zone and --unknown are mutually exclusive.");
            }
        }

        return new SetDateCommandInput(args, patternStr, dateTime, zoneId, rename, unknownOnly, fixZone, forceField);
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
        options.addOption(ZONE_ID);
        options.addOption(RENAME);
        options.addOption(UNKNOWN_ONLY);
        options.addOption(FIX_ZONE);
        options.addOption(FORCE_FIELD);
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
