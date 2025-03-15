package sk.kubisoft.exifutils.core.metadata;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Singleton
public class ExifDateSetter {

    // Constants for EXIF Image tags
    private static final String DATE_TIME_TAG = "DateTime";
    private static final String DATE_TIME_ORIGINAL_TAG = "DateTimeOriginal";
    private static final String OFFSET_TIME_TAG = "OffsetTime";
    private static final String OFFSET_TIME_ORIGINAL_TAG = "OffsetTimeOriginal";
    // Constants for EXIF Video tags
    private static final String CREATE_DATE_UTC_TAG = "CreateDate";
    private static final String CREATION_DATE_OFFSET_TAG = "CreationDate";

    private final Console console;

    private final MetaDataHandlerFactory metaDataHandlerFactory;

    @Inject
    public ExifDateSetter(Console console, MetaDataHandlerFactory metaDataHandlerFactory) {
        this.console = console;
        this.metaDataHandlerFactory = metaDataHandlerFactory;
    }

    public boolean needsDateTimeSet(AnalyzedMediaFile mediaFile) {
        var metadata = mediaFile.getMetadata();
        return switch (mediaFile.getMediaType()) {
            case IMAGE -> {
                var dateTimeOriginal = metadata.get(DATE_TIME_ORIGINAL_TAG);
                var offsetTimeOriginal = metadata.get(OFFSET_TIME_ORIGINAL_TAG);
                yield StringUtils.isBlank(dateTimeOriginal) || StringUtils.isBlank(offsetTimeOriginal);
            }
            case VIDEO -> {
                var createDate = metadata.get(CREATE_DATE_UTC_TAG);
                var creationDate = metadata.get(CREATION_DATE_OFFSET_TAG);
                yield StringUtils.isBlank(createDate) || StringUtils.isBlank(creationDate);
            }
            default -> throw new IllegalArgumentException("Unsupported media type: " + mediaFile.getMediaType());
        };
    }

    public void setDateTime(List<SetDateAction> setDateActionList) {
        console.println("Setting datetime to files...");
        try (var metaDataSetter = metaDataHandlerFactory.create()) {
            for (int i = 0; i < setDateActionList.size(); i++) {
                var action = setDateActionList.get(i);

                if (console.isVerbose()) {
                    console.println("Setting datetime for file %d of %d: %s", i + 1, setDateActionList.size(), action.file());
                } else {
                    console.progress("Setting datetime for file %d of %d: %s", i + 1, setDateActionList.size(), action.file());
                }

                setDateTime(metaDataSetter, action.file(), action.mediaType(), action.dateTime());

                if (console.isVerbose()) {
                    console.println(""); // Append newline after each file in verbose mode for clarity
                }
            }
            if (!console.isVerbose()) {
                console.progress(""); // Clear progress line
            }
            console.println("Setting datetime to files finished.");
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }
    }

    private void setDateTime(MetaDataHandler metaDataSetter, Path file, MediaType mediaType, MediaDateTime mediaDateTime) {
        Map<String, String> newTags = switch (mediaType) {
            case IMAGE -> createImageTags(mediaDateTime);
            case VIDEO -> createVideoTags(mediaDateTime);
            default -> throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        };

        metaDataSetter.setMetaDataTags(file, newTags);
    }

    private Map<String, String> createImageTags(MediaDateTime mediaDateTime) {
        // For images, we need to set both DateTimeOriginal and OffsetTimeOriginal as specified by Exif version 2.31 (July 2016).
        // Related tags are: "OffsetTime", "OffsetTimeOriginal" and "OffsetTimeDigitized".
        var dateTime = formatToLocalExifDateTime(mediaDateTime.getLocalDateTime());
        var offsetTime = mediaDateTime.getZoneOffset().getId();
        return Map.of(
                DATE_TIME_TAG, dateTime,
                DATE_TIME_ORIGINAL_TAG, dateTime,
                OFFSET_TIME_TAG, offsetTime,
                OFFSET_TIME_ORIGINAL_TAG, offsetTime
        );
    }

    private Map<String, String> createVideoTags(MediaDateTime mediaDateTime) {
        // For videos, it's a little trickier, because, by convention, the date time is stored in "CreateDate" tag, for example: 2023:08:31 15:10:31
        // However, the tag stores the date by convention in UTC time zone, which is tricky, because then we cannot infer the time zone easily,
        // because videos does not have the "OffsetTime" tag.
        // However, iPhones stores the full offset date and time in "CreationDate" tag, so we can use that, for example: 2023:08:31 18:10:31+03:00
        var utcDateTime = formatToLocalExifDateTime(mediaDateTime.getUtcDateTime().toLocalDateTime());
        var offsetDateTime = formatToOffsetExifDateTime(mediaDateTime.getDateTime());

        return Map.of(
                CREATE_DATE_UTC_TAG, utcDateTime,
                CREATION_DATE_OFFSET_TAG, offsetDateTime
        );
    }

    private String formatToLocalExifDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    private String formatToOffsetExifDateTime(OffsetDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ssXXX");
        return dateTime.format(formatter);
    }
}
