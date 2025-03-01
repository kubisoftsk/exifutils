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
                createFormatter("yyyy-MM-dd_HH-mm-ss"),
                createFormatter("yyyy-MM-dd HH-mm-ss"),

                // IMG/VID formats
                createFormatter("'IMG'yyyyMMddHHmmss"),
                createFormatter("'IMG_'yyyyMMddHHmmss"),
                createFormatter("'IMG_'yyyyMMdd'_'HHmmss"),
                createFormatter("'IMG-'yyyyMMddHHmmss"),
                createFormatter("'VID'yyyyMMddHHmmss"),
                createFormatter("'VID_'yyyyMMddHHmmss"),
                createFormatter("'VID_'yyyyMMdd'_'HHmmss"),
                createFormatter("'VID-'yyyyMMddHHmmss"),

                // iPhone format
                createFormatter("uu-MM-dd HH-mm-ss"),

                // Other
                createFormatter("'download_'yyyyMMdd'_'HHmmss"),
                createFormatter("'IMG_'yyyy'_'MM'_'dd'_'HH'_'mm'_'ss"),
                createFormatter("'WhatsApp Image 'yyyy-MM-dd' at 'HH.mm.ss")
        );
    }

    private static FormatterWithLength createFormatter(String pattern) {
        var formatter = DateTimeFormatter.ofPattern(pattern);
        var length = pattern.replaceAll("'", "").length();
        return new FormatterWithLength(formatter, length);
    }

    public Optional<LocalDateTime> analyzeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Optional.empty();
        }

        // Remove any trailing spaces and common suffixes
        String input = fileName.trim();

        for (FormatterWithLength formatterWithLength : FORMATTERS) {
            try {
                // Try to find a substring that matches our date pattern
                int maxLength = Math.min(input.length(), formatterWithLength.length());
                String substring = input.substring(0, maxLength);
                try {
                    var result = LocalDateTime.parse(substring, formatterWithLength.formatter());
                    return Optional.of(result);
                } catch (DateTimeParseException e) {
                    // Continue trying with shorter substring
                    continue;
                }
            } catch (Exception e) {
                // Try next formatter
                continue;
            }
        }
        return Optional.empty();
    }

    record FormatterWithLength(DateTimeFormatter formatter, int length) {
    }
}
