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

}
