package sk.kubisoft.exifutils.core.file;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public final class FileFilter {

    public static final FileFilter NONE = new FileFilter(List.of(), List.of());

    private final List<PathMatcher> includeMatchers;
    private final List<PathMatcher> excludeMatchers;

    private FileFilter(List<PathMatcher> includeMatchers, List<PathMatcher> excludeMatchers) {
        this.includeMatchers = includeMatchers;
        this.excludeMatchers = excludeMatchers;
    }

    public static FileFilter of(List<String> includePatterns, List<String> excludePatterns) {
        if (includePatterns.isEmpty() && excludePatterns.isEmpty()) {
            return NONE;
        }
        return new FileFilter(toMatchers(includePatterns), toMatchers(excludePatterns));
    }

    private static List<PathMatcher> toMatchers(List<String> patterns) {
        return patterns.stream().map(FileFilter::toMatcher).toList();
    }

    private static PathMatcher toMatcher(String pattern) {
        String syntaxAndPattern = pattern.startsWith("regex:") ? pattern : "glob:" + pattern;
        try {
            return FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid filter pattern: '" + pattern + "'. " + e.getMessage());
        }
    }

    public boolean matches(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return true;
        }
        boolean included = includeMatchers.isEmpty() || includeMatchers.stream().anyMatch(m -> m.matches(fileName));
        if (!included) {
            return false;
        }
        return excludeMatchers.stream().noneMatch(m -> m.matches(fileName));
    }
}
