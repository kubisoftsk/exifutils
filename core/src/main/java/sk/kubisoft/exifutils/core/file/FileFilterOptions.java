package sk.kubisoft.exifutils.core.file;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.List;

public final class FileFilterOptions {

    private static final Option INCLUDE = Option.builder()
            .option("i")
            .longOpt("include")
            .hasArg()
            .argName("PATTERN")
            .desc("Only include files whose name matches PATTERN (glob by default, or 'regex:' prefix for regex). Repeatable.")
            .build();

    private static final Option EXCLUDE = Option.builder()
            .option("x")
            .longOpt("exclude")
            .hasArg()
            .argName("PATTERN")
            .desc("Exclude files whose name matches PATTERN (glob by default, or 'regex:' prefix for regex). Applied after --include. Repeatable.")
            .build();

    private FileFilterOptions() {
    }

    public static void addTo(Options options) {
        options.addOption(INCLUDE);
        options.addOption(EXCLUDE);
    }

    public static FileFilter parse(CommandLine cmd) throws ParseException {
        try {
            return FileFilter.of(optionValues(cmd, INCLUDE), optionValues(cmd, EXCLUDE));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage());
        }
    }

    private static List<String> optionValues(CommandLine cmd, Option option) {
        String[] values = cmd.getOptionValues(option.getOpt());
        return values == null ? List.of() : Arrays.asList(values);
    }
}
