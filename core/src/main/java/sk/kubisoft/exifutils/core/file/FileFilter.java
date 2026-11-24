package sk.kubisoft.exifutils.core.file;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Filters files by file name (not the full path), ignoring case.
 */
public final class FileFilter {

    public static final FileFilter NONE = new FileFilter(List.of(), List.of());

    private static final String REGEX_PREFIX = "regex:";
    private static final String GLOB_PREFIX = "glob:";

    private final List<Pattern> includePatterns;
    private final List<Pattern> excludePatterns;

    private FileFilter(List<Pattern> includePatterns, List<Pattern> excludePatterns) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
    }

    public static FileFilter of(List<String> includePatterns, List<String> excludePatterns) {
        if (includePatterns.isEmpty() && excludePatterns.isEmpty()) {
            return NONE;
        }
        return new FileFilter(compile("--include", includePatterns), compile("--exclude", excludePatterns));
    }

    private static List<Pattern> compile(String optionName, List<String> patterns) {
        return patterns.stream().map(pattern -> compile(optionName, pattern)).toList();
    }

    private static Pattern compile(String optionName, String pattern) {
        try {
            return compile(pattern);
        } catch (PatternSyntaxException e) {
            String position = e.getIndex() >= 0 ? " (near index " + e.getIndex() + ")" : "";
            throw new IllegalArgumentException("Invalid %s pattern '%s': %s%s"
                    .formatted(optionName, pattern, e.getDescription(), position));
        }
    }

    private static Pattern compile(String pattern) {
        if (pattern.startsWith(REGEX_PREFIX)) {
            return compileRegex(pattern.substring(REGEX_PREFIX.length()));
        }
        String glob = pattern.startsWith(GLOB_PREFIX) ? pattern.substring(GLOB_PREFIX.length()) : pattern;
        checkNotBlank(glob);
        return GlobPattern.compile(glob, File.separatorChar);
    }

    private static Pattern compileRegex(String regex) {
        checkNotBlank(regex);
        if (regex.indexOf('/') >= 0) {
            throw new PatternSyntaxException(GlobPattern.SEPARATOR_ERROR, regex, regex.indexOf('/'));
        }
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private static void checkNotBlank(String pattern) {
        if (pattern.isBlank()) {
            throw new PatternSyntaxException("Pattern is empty", pattern, -1);
        }
    }

    public boolean matches(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return true;
        }
        String name = fileName.toString();
        boolean included = includePatterns.isEmpty() || matchesAny(includePatterns, name);
        return included && !matchesAny(excludePatterns, name);
    }

    private static boolean matchesAny(List<Pattern> patterns, String name) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(name).matches());
    }
}
