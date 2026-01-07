package sk.kubisoft.exifutils.core.file;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Singleton
public class FileNameAnalyzer {

    private static final List<FormatterWithLength> FORMATTERS = createFormatters();

    @Inject
    public FileNameAnalyzer() {
    }

    private static List<FormatterWithLength> createFormatters() {
        return List.of(
                // Basic format YYYYMMDD_HHMMSS with optional separator
                createFormatter("yyyyMMddHHmmss"),
                createFormatter("yyyyMMdd_HHmmss"),
                createFormatter("yyyyMMdd-HHmmss"),
                createFormatter("yyyy-MM-dd_HHmmss"),
                createFormatter("yyyy-MM-dd_HH-mm-ss"),
                createFormatter("yyyy-MM-dd HHmmss"),
                createFormatter("yyyy-MM-dd HH-mm-ss"),

                // IMG/VID formats
                createFormatter("'IMG'yyyyMMddHHmmss"),
                createFormatter("'IMG_'yyyyMMddHHmmss"),
                createFormatter("'IMG_'yyyyMMdd'_'HHmmss"),
                createFormatter("'IMG-'yyyyMMddHHmmss"),
                createFormatter("'Img 'yyyyMMdd' 'HHmmss"),
                createFormatter("'IMG_'yyyy'_'MM'_'dd'_'HH'_'mm'_'ss"),
                createFormatter("'VID'yyyyMMddHHmmss"),
                createFormatter("'VID_'yyyyMMddHHmmss"),
                createFormatter("'VID_'yyyyMMdd'_'HHmmss"),
                createFormatter("'VID-'yyyyMMddHHmmss"),

                // iPhone format
                createFormatter("uu-MM-dd HH-mm-ss"),

                // Other
                createFormatter("'download_'yyyyMMdd'_'HHmmss"),
                createFormatter("'WhatsApp Image 'yyyy-MM-dd' at 'HH.mm.ss")
        );
    }

    private static FormatterWithLength createFormatter(String pattern) {
        var formatter = DateTimeFormatter.ofPattern(pattern);
        var length = pattern.replaceAll("'", "").length();
        return new FormatterWithLength(formatter, length);
    }

    public Optional<LocalDateTime> analyzeFileName(String fileName) {
        return analyzeFileName(fileName, null);
    }

    public Optional<LocalDateTime> analyzeFileName(String fileName, String userPattern) {
        if (fileName == null || fileName.isEmpty()) {
            return Optional.empty();
        }

        // Remove any trailing spaces and common suffixes
        String input = fileName.trim();

        // If user supplied a pattern, use only that (strict mode - no fallback)
        if (userPattern != null && !userPattern.isBlank()) {
            return tryParseWithUserPattern(input, userPattern);
        }

        // Use default hardcoded patterns
        for (FormatterWithLength formatterWithLength : FORMATTERS) {
            var result = tryParse(input, formatterWithLength.formatter(), formatterWithLength.length());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private Optional<LocalDateTime> tryParseWithUserPattern(String input, String userPattern) {
        try {
            var formatterWithLength = createFormatter(userPattern);
            return tryParse(input, formatterWithLength.formatter(), formatterWithLength.length());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDateTime> tryParse(String input, DateTimeFormatter formatter, int patternLength) {
        try {
            int maxLength = Math.min(input.length(), patternLength);
            String substring = input.substring(0, maxLength);
            var result = LocalDateTime.parse(substring, formatter);
            return Optional.of(result);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    record FormatterWithLength(DateTimeFormatter formatter, int length) {
    }
}
