package sk.kubisoft.exifutils.core.analysis;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MediaDateExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MediaDateExtractor.class);

    private static final String GPS_LATITUDE_TAG = "GPSLatitude";
    private static final String GPS_LONGITUDE_TAG = "GPSLongitude";

    private final ConfigService configService;

    @Inject
    public MediaDateExtractor(ConfigService configService) {
        this.configService = configService;
    }

    public Optional<MediaDateTime> extractCreationDate(MediaFile mediaFile) {
        var mediaType = mediaFile.mediaType();
        var metadata = mediaFile.metadata();

        // Order of precedence for date fields
        OffsetDateTimeField[] dateFields = {
                new OffsetDateTimeField("DateTimeOriginal", "OffsetTimeOriginal"),
                new OffsetDateTimeField("CreationDate", "OffsetTime"),
                new OffsetDateTimeField("CreateDate", "OffsetTime"),
                new OffsetDateTimeField("MediaCreateDate", null),
        };

        Map<OffsetDateTimeField, MediaDateTime> dateFieldsWithDate = new LinkedHashMap<>();
        for (var dateField : dateFields) {
            String dateStr = metadata.get(dateField.dateField());
            String offsetStr = (dateField.offsetField() == null) ? null : metadata.get(dateField.offsetField());

            if (StringUtils.isBlank(dateStr)) {
                continue;
            }

            try {
                var mediaDateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);
                mediaDateTime.ifPresent(dateTime -> dateFieldsWithDate.put(dateField, dateTime));
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse date from {} field: '{}': {}", dateField.dateField(), dateStr, e.getMessage());
            } catch (Exception e) {
                logger.error("Error parsing date from {} field: {}", dateField.dateField(), dateStr, e);
            }
        }

        // Check parsed dates and return the first valid one with offset present
        Optional<MediaDateTime> firstWithOffset = dateFieldsWithDate.values().stream()
                .filter(MediaDateTime::hasZoneOffset)
                .findFirst();

        if (firstWithOffset.isPresent()) {
            return firstWithOffset;
        }

        // If no valid date with offset found, return the first valid date without offset
        var dateWithoutOffsetOptional = dateFieldsWithDate.values().stream()
                .findFirst();

        // Return immediately if no date found
        if (dateWithoutOffsetOptional.isEmpty()) {
            return Optional.empty();
        }

        // Now some guesswork for zone offset for incomplete metadata
        var dateWithoutOffset = dateWithoutOffsetOptional.get();
        var localDateWithoutOffset = dateWithoutOffset.getLocalDateTime();
        // use system default offset as a guess
        ZoneOffset offsetToUse;
        var gpsZoneOffsetOptional = getGpsZoneOffset(dateWithoutOffset.getLocalDateTime(), metadata);
        offsetToUse = gpsZoneOffsetOptional.orElseGet(() -> getDefaultZoneOffset(localDateWithoutOffset));
        if (mediaType == MediaType.VIDEO) {
            // assume the video date is in UTC time, this is important for videos, because historically
            // quick time videos has the date in UTC time, so we must convert it to local time with guessed offset
            OffsetDateTime utcDateTime = localDateWithoutOffset.atOffset(ZoneOffset.UTC);
            var localTimeAtOffsetSameInstant = utcDateTime.withOffsetSameInstant(offsetToUse).toLocalDateTime();
            return Optional.of(new MediaDateTime(localTimeAtOffsetSameInstant, offsetToUse));
        } else if (mediaType == MediaType.IMAGE) {
            // similar to video, but we assume the image date is in local time, so we don't need to convert it, just use it
            return Optional.of(new MediaDateTime(localDateWithoutOffset, offsetToUse));
        } else {
            throw new IllegalArgumentException("Unknown media type: " + mediaType);
        }
    }

    private ZoneOffset getDefaultZoneOffset(LocalDateTime localDateWithoutOffset) {
        var config = configService.getConfig();
        var dateTimeConfig = config.getDateTime();
        if (dateTimeConfig != null && dateTimeConfig.getTimeZone() != null) {
            ZoneId zoneId = ZoneId.of(dateTimeConfig.getTimeZone());
            return zoneId.getRules().getOffset(localDateWithoutOffset);
        }
        // fallback to system default
        return ZoneOffset.systemDefault().getRules().getOffset(localDateWithoutOffset);
    }

    private Optional<ZoneOffset> getGpsZoneOffset(LocalDateTime localDateTime, Map<String, String> metadata) {
        String gpsLatitude = metadata.get(GPS_LATITUDE_TAG);
        String gpsLongitude = metadata.get(GPS_LONGITUDE_TAG);

        if (StringUtils.isNotBlank(gpsLatitude) && StringUtils.isNotBlank(gpsLongitude)) {
            TimeZoneEngine engine = TimeZoneEngine.initialize();
            Optional<ZoneId> maybeZoneId = engine.query(Double.parseDouble(gpsLatitude), Double.parseDouble(gpsLongitude));
            return maybeZoneId.map(zoneId -> {
                logger.debug("Found zoneId from GPS coordinates: {}", zoneId);
                return zoneId.getRules().getOffset(localDateTime);
            });
        } else {
            return Optional.empty();
        }
    }

    record OffsetDateTimeField(String dateField, String offsetField) {
    }
}

