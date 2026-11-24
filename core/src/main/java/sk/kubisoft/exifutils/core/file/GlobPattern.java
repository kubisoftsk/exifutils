package sk.kubisoft.exifutils.core.file;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Converts a glob to a case-insensitive {@link Pattern} for matching a single file name.
 * <p>
 * The syntax is the one of {@link java.nio.file.FileSystem#getPathMatcher(String)}. The JDK matcher itself cannot be
 * used, because its case sensitivity depends on the platform. Lower-casing the glob would change the meaning of
 * character ranges, so the glob is converted to a regex and case is ignored by the regex engine.
 */
final class GlobPattern {

    static final String SEPARATOR_ERROR = "Only the file name is matched, so the pattern must not contain a path separator";

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;

    // Characters with a special meaning in a glob that can also be a part of a Windows file name
    private static final String WINDOWS_ESCAPABLE = "[]{},";

    private final String glob;
    private final int[] chars;
    private final boolean windows;
    private final StringBuilder regex = new StringBuilder();
    private int pos = 0;

    private GlobPattern(String glob, char separator) {
        this.glob = glob;
        this.windows = separator == '\\';
        this.chars = glob.codePoints().toArray();
    }

    /**
     * @param separator path separator of the platform. On Windows, a backslash that does not escape anything that can be
     *                  in a file name is taken as a path separator.
     * @throws PatternSyntaxException if the glob is not valid or cannot match any file name
     */
    static Pattern compile(String glob, char separator) {
        return Pattern.compile(new GlobPattern(glob, separator).toRegex(), FLAGS);
    }

    private String toRegex() {
        boolean inGroup = false;
        while (pos < chars.length) {
            int c = chars[pos++];
            switch (c) {
                case '*' -> {
                    // "**" has no special meaning in a file name
                    while (pos < chars.length && chars[pos] == '*') {
                        pos++;
                    }
                    regex.append(".*");
                }
                case '?' -> regex.append('.');
                case '[' -> appendBrackets();
                case '{' -> {
                    if (inGroup) {
                        throw error("Cannot nest groups");
                    }
                    inGroup = true;
                    regex.append("(?:");
                }
                case '}' -> {
                    if (inGroup) {
                        inGroup = false;
                        regex.append(')');
                    } else {
                        appendLiteral(c);
                    }
                }
                case ',' -> {
                    if (inGroup) {
                        regex.append('|');
                    } else {
                        appendLiteral(c);
                    }
                }
                case '\\' -> {
                    if (pos == chars.length) {
                        throw error("No character to escape");
                    }
                    int escaped = chars[pos++];
                    if (windows && WINDOWS_ESCAPABLE.indexOf(escaped) < 0) {
                        throw error(SEPARATOR_ERROR);
                    }
                    appendLiteral(escaped);
                }
                default -> appendLiteral(c);
            }
        }
        if (inGroup) {
            throw error("Missing '}'");
        }
        return regex.toString();
    }

    private void appendBrackets() {
        regex.append('[');
        if (pos < chars.length && chars[pos] == '!') {
            regex.append('^');
            pos++;
        }
        int start = pos;
        int rangeStart = -1;
        while (true) {
            if (pos == chars.length) {
                throw error("Missing ']'");
            }
            int c = chars[pos++];
            if (c == ']') {
                if (pos - 1 == start) {
                    throw error("Empty brackets");
                }
                regex.append(']');
                return;
            }
            boolean first = pos - 1 == start;
            boolean last = pos < chars.length && chars[pos] == ']';
            if (c == '-' && !first && !last && pos < chars.length) {
                int rangeEnd = chars[pos++];
                if (rangeStart < 0 || rangeEnd < rangeStart) {
                    throw error("Invalid range");
                }
                regex.append('-');
                appendLiteral(rangeEnd);
                rangeStart = -1;
            } else {
                appendLiteral(c);
                rangeStart = c;
            }
        }
    }

    private void appendLiteral(int c) {
        if (c == '/' || (windows && c == '\\')) {
            throw error(SEPARATOR_ERROR);
        }
        if (Character.isLetterOrDigit(c)) {
            regex.appendCodePoint(c);
        } else {
            regex.append(String.format("\\x{%x}", c));
        }
    }

    private PatternSyntaxException error(String description) {
        return new PatternSyntaxException(description, glob, pos - 1);
    }
}
