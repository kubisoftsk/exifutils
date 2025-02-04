package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ExifDateParser {

    private static final Logger logger = LoggerFactory.getLogger(ExifDateParser.class);


    // Format for dates without offset
    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    // Pattern to match potential offset in date string
    private static final Pattern OFFSET_PATTERN =
            Pattern.compile("([0-9: ]+?)(?:([+-][0-9]{2}:?[0-9]{2})|Z)?$");

    @Inject
    public ExifDateParser() {
    }

    /**
     * Parses EXIF date string with optional offset information.
     *
     * @param dateStr the date string to parse
     * @param offsetStr additional offset string (may be null)
     * @return Optional containing the parsed OffsetDateTime, or empty if input is invalid
     * @throws DateTimeParseException if the date string cannot be parsed
     */
    public Optional<ExifDateTime> parseExifDate(String dateStr, String offsetStr) throws DateTimeParseException {
        // Check for blank or invalid date string
        if (dateStr == null || dateStr.isBlank() || "0000:00:00 00:00:00".equals(dateStr.trim())) {
            return Optional.empty();
        }

        // Remove any fractional seconds
        String cleanDateStr = dateStr.split("\\.")[0].trim();

        // Try to extract offset from date string if present
        Matcher matcher = OFFSET_PATTERN.matcher(cleanDateStr);
        if (!matcher.matches()) {
            throw new DateTimeParseException("Invalid date format", cleanDateStr, 0);
        }

        String datePartStr = matcher.group(1).trim();
        String embeddedOffset = matcher.group(2);

        // Parse the date part
        LocalDateTime dateTime = LocalDateTime.parse(datePartStr, EXIF_DATE_FORMAT);

        // Determine offset priority: embedded offset > provided offset > UTC
        ZoneOffset offset = null;
        try {
            if (embeddedOffset != null) {
                // Normalize offset format (remove colon if present)
                String normalizedOffset = embeddedOffset.replace(":", "");
                offset = ZoneOffset.of(normalizedOffset);
            } else if (offsetStr != null && !offsetStr.isBlank()) {
                // Normalize provided offset format
                String normalizedOffset = offsetStr.replace(":", "");
                offset = ZoneOffset.of(normalizedOffset);
            }
        } catch (DateTimeException e) {
            // If offset parsing fails, keep offset null
        }

        return Optional.of(new ExifDateTime(dateTime, offset));
    }
}
