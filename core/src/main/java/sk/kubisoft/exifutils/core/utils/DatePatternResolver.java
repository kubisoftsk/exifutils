package sk.kubisoft.exifutils.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves date patterns in strings using the ${date,FORMAT} syntax.
 * The FORMAT follows Java's DateTimeFormatter pattern specifications.
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 */
public final class DatePatternResolver {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\$\\{date,([^}]+)}");

    private DatePatternResolver() {
        // Utility class
    }

    /**
     * Resolves all ${date,FORMAT} patterns in the input string using the provided date.
     *
     * @param pattern the pattern string containing ${date,FORMAT} placeholders
     * @param dateTime the date to use for formatting
     * @return the resolved string with all date placeholders replaced
     */
    public static String resolve(String pattern, LocalDateTime dateTime) {
        Matcher matcher = DATE_PATTERN.matcher(pattern);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String format = matcher.group(1);
            String formatted = dateTime.format(DateTimeFormatter.ofPattern(format));
            matcher.appendReplacement(result, Matcher.quoteReplacement(formatted));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
