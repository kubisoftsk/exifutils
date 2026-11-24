package sk.kubisoft.exifutils.core.file;

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
        return new FileFilter(compile(includePatterns), compile(excludePatterns));
    }

    private static List<Pattern> compile(List<String> patterns) {
        return patterns.stream().map(FileFilter::compile).toList();
    }

    private static Pattern compile(String pattern) {
        try {
            if (pattern.startsWith(REGEX_PREFIX)) {
                return Pattern.compile(pattern.substring(REGEX_PREFIX.length()),
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
            return GlobPattern.compile(pattern.startsWith(GLOB_PREFIX) ? pattern.substring(GLOB_PREFIX.length()) : pattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter pattern: '" + pattern + "'. " + e.getMessage());
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
