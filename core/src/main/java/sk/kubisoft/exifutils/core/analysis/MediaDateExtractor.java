package sk.kubisoft.exifutils.core.analysis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MediaDateExtractor {

    /*
       Tip for improvement: OnePlus videos does not contain offset in exif data. However, it can be guessed
       from the file name. For example, the file name "VID_20210101_120000.mp4" can be parsed to extract the
         date and time, and the offset can be guessed from the file name.
       Second guess can be from file modify date, where the offset is present. However this is dangerous and
       it shoudl be checked if it is the same minute / second as in Created date. Then it can be used as offset.

       Another thing I found is that older oneplus phones do not have offset in exif data in PHOTOS, however
       they have it in local time, so it is still useful for sorting. Problem is in videos, because there the date
       is for some reason stored in UTC time, so it may cause problems when photos are taken around midnight.
     */

    private static final Logger logger = LoggerFactory.getLogger(MediaDateExtractor.class);

    @Inject
    public MediaDateExtractor() {
    }

    public Optional<MediaDateTime> extractCreationDate(MediaFile mediaFile) {
        return extractDate(mediaFile.metadata());
    }

    public Optional<MediaDateTime> extractDate(Map<String, String> metadata) {
        // Order of precedence for date fields
        OffsetDateTimeField[] dateFields = {
                new OffsetDateTimeField("CreationDate", "OffsetTime"),
                new OffsetDateTimeField("CreateDate", "OffsetTime"),
                new OffsetDateTimeField("DateTimeOriginal", "OffsetTimeOriginal"),
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
        return dateFieldsWithDate.values().stream()
                .findFirst();
    }

    record OffsetDateTimeField(String dateField, String offsetField) {
    }
}