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

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;

    private final String glob;
    private final int[] chars;
    private final StringBuilder regex = new StringBuilder();
    private int pos = 0;

    private GlobPattern(String glob) {
        this.glob = glob;
        this.chars = glob.codePoints().toArray();
    }

    /**
     * @throws PatternSyntaxException if the glob is not valid
     */
    static Pattern compile(String glob) {
        return Pattern.compile(new GlobPattern(glob).toRegex(), FLAGS);
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
                    appendLiteral(chars[pos++]);
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
