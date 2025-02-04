package sk.kubisoft.exifutils.core.analysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ExifDateExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ExifDateExtractor.class);

    private final Console console;

    private final ExifDateParser exifDateParser;

    @Inject
    public ExifDateExtractor(Console console, ExifDateParser exifDateParser) {
        this.console = console;
        this.exifDateParser = exifDateParser;
    }

    // Order of precedence for date fields
    private static final OffsetDateTimeField[] DATE_TIME_FIELDS = {
            new OffsetDateTimeField("DateTimeOriginal", "OffsetTimeOriginal"),
            new OffsetDateTimeField("CreationDate", "OffsetTime"),
            new OffsetDateTimeField("CreateDate", "OffsetTime"),
            new OffsetDateTimeField("MediaCreateDate", null),
    };

    public Optional<ExifDateTime> extractCreationDate(Map<String, String> metadata) {
        Map<OffsetDateTimeField, ExifDateTime> dateFieldsWithDate = new LinkedHashMap<>();
        for (var dateField : DATE_TIME_FIELDS) {
            String dateStr = metadata.get(dateField.dateField());
            String offsetStr = (dateField.offsetField() == null) ? null : metadata.get(dateField.offsetField());

            if (StringUtils.isBlank(dateStr)) {
                continue;
            }

            try {
                var exifDateTime = exifDateParser.parseExifDate(dateStr, offsetStr);
                exifDateTime.ifPresent(dateTime -> dateFieldsWithDate.put(dateField, dateTime));
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse date from {} field: '{}': {}", dateField.dateField(), dateStr, e.getMessage());
            } catch (Exception e) {
                logger.error("Error parsing date from {} field: {}", dateField.dateField(), dateStr, e);
            }
        }

        // Verbose and debug logging
        console.verboseln("Found dates in following EXIF fields:");
        int maxFieldLength = dateFieldsWithDate.keySet().stream()
                .mapToInt(field -> field.toString().length())
                .max()
                .orElse(0);
        dateFieldsWithDate.forEach((field, date) -> {
            var paddedField = StringUtils.rightPad(field.toString(), maxFieldLength);
            console.verboseln("  %s: %s", paddedField, date);
        });

        // Check parsed dates and return the first valid one with offset present
        Optional<ExifDateTime> firstWithOffset = dateFieldsWithDate.values().stream()
                .filter(exifDateTime -> exifDateTime.zoneOffset() != null)
                .findFirst();

        if (firstWithOffset.isPresent()) {
            console.verboseln("Found date with offset within EXIF tags: %s", firstWithOffset.get());
            return firstWithOffset;
        }

        // If no valid date with offset found, return the first valid date without offset,
        // or empty if no valid date found
        return dateFieldsWithDate.values().stream()
                .findFirst();
    }

    private record OffsetDateTimeField(String dateField, String offsetField) {
        @Override
        public String toString() {
            if (offsetField == null) {
                return dateField;
            } else {
                return dateField + "+" + offsetField;
            }
        }
    }
}
