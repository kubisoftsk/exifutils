package sk.kubisoft.exifutils.core.analysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.device.DateTimeField;
import sk.kubisoft.exifutils.core.analysis.device.DeviceProfile;
import sk.kubisoft.exifutils.core.analysis.device.DeviceProfileService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ExifDateExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ExifDateExtractor.class);

    private final Console console;

    private final ExifDateParser exifDateParser;

	private final DeviceProfileService deviceProfileService;

    @Inject
    public ExifDateExtractor(Console console, ExifDateParser exifDateParser, DeviceProfileService deviceProfileService) {
        this.console = console;
        this.exifDateParser = exifDateParser;
		this.deviceProfileService = deviceProfileService;
    }

    public Optional<ExifDateTime> extractCreationDate(MediaType mediaType, Map<String, String> metadata) {
        return extractCreationDate(mediaType, metadata, null);
    }

    public Optional<ExifDateTime> extractCreationDate(MediaType mediaType, Map<String, String> metadata, String forceField) {
        if (StringUtils.isNotBlank(forceField)) {
            return extractFromForcedField(metadata, forceField);
        }

		DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);
		console.verboseln("Using device profile: %s", deviceProfile.getName());

		List<DateTimeField> dateTimeFields = switch (mediaType) {
			case IMAGE -> deviceProfile.getImageFields();
			case VIDEO -> deviceProfile.getVideoFields();
			default -> throw new IllegalArgumentException("Unsupported media type: " + mediaType);
		};

		ExifDateTime exifDateTime = null;
        for (var dateField : dateTimeFields) {
            String dateStr = metadata.get(dateField.getDateField());
            String offsetStr = (dateField.getOffsetField() == null) ? null : metadata.get(dateField.getOffsetField());

            if (StringUtils.isBlank(dateStr)) {
                continue;
            }

            try {
                var exifDateTimeOptional = exifDateParser.parseExifDate(dateStr, dateField.isLocalTime(), offsetStr);
				if (exifDateTimeOptional.isPresent()) {
					exifDateTime = exifDateTimeOptional.get();
					console.verboseln("Found date in EXIF field %s: %s", dateField.getDateField(), exifDateTime);
					break;
				}
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse date from {} field: '{}': {}", dateField.getDateField(), dateStr, e.getMessage());
            } catch (Exception e) {
                logger.error("Error parsing date from {} field: {}", dateField.getDateField(), dateStr, e);
            }
        }

		if (exifDateTime != null) {
			return Optional.of(exifDateTime);
		} else {
			console.verboseln("No valid date found in EXIF tags");
			return Optional.empty();
		}
    }

    private Optional<ExifDateTime> extractFromForcedField(Map<String, String> metadata, String forceField) {
        console.verboseln("Using forced field: %s", forceField);
        String dateStr = metadata.get(forceField);

        if (StringUtils.isBlank(dateStr)) {
            console.verboseln("No value found in forced field: %s", forceField);
            return Optional.empty();
        }

        try {
            // Parse as local time with no offset field - timezone resolution will be handled by MediaAnalyzer
            var exifDateTimeOptional = exifDateParser.parseExifDate(dateStr, true, null);
            if (exifDateTimeOptional.isPresent()) {
                console.verboseln("Found date in forced field %s: %s", forceField, exifDateTimeOptional.get());
                return exifDateTimeOptional;
            }
        } catch (DateTimeParseException e) {
            logger.warn("Could not parse date from forced field {}: '{}': {}", forceField, dateStr, e.getMessage());
        } catch (Exception e) {
            logger.error("Error parsing date from forced field {}: {}", forceField, dateStr, e);
        }

        return Optional.empty();
    }

}
