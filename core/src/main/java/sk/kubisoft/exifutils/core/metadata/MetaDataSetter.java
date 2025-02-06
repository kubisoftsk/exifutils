package sk.kubisoft.exifutils.core.metadata;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.ExifToolOptions;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardOptions;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MetaDataSetter implements AutoCloseable {

    private static final String DATE_TIME_TAG = "DateTime";
    private static final String DATE_TIME_ORIGINAL_TAG = "DateTimeOriginal";
    private static final String OFFSET_TIME_TAG = "OffsetTime";
    private static final String OFFSET_TIME_ORIGINAL_TAG = "OffsetTimeOriginal";

    private static final String CREATE_DATE_UTC_TAG = "CreateDate";
    private static final String CREATION_DATE_TAG = "CreationDate";

    private final ExifTool exifTool;

    private final ExifToolOptions exifToolOptions = StandardOptions.builder()
            .withOverwriteOriginal()
            .build();

    public MetaDataSetter(String exifToolPath) {
        this.exifTool = new ExifToolBuilder()
                .withPath(exifToolPath)
                .enableStayOpen()  // Performance optimization for multiple files
                .build();
    }

    public void setDateTime(Path file, MediaType mediaType, MediaDateTime mediaDateTime) {
        Map<Tag, String> newTags = switch (mediaType) {
            case IMAGE -> createImageTags(mediaDateTime);
            case VIDEO -> createVideoTags(mediaDateTime);
            default -> throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        };

        try {
            exifTool.setImageMeta(file.toFile(), exifToolOptions, newTags);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<Tag, String> createImageTags(MediaDateTime mediaDateTime) {
        // For images, we need to set both DateTimeOriginal and OffsetTimeOriginal as specified by Exif version 2.31 (July 2016).
        // Related tags are: "OffsetTime", "OffsetTimeOriginal" and "OffsetTimeDigitized".
        var dateTime = formatToLocalExifDateTime(mediaDateTime.getLocalDateTime());
        var offsetTime = mediaDateTime.getZoneOffset().getId();
        return Map.of(
                new UnspecifiedTag(DATE_TIME_TAG), dateTime,
                new UnspecifiedTag(DATE_TIME_ORIGINAL_TAG), dateTime,
                new UnspecifiedTag(OFFSET_TIME_TAG), offsetTime,
                new UnspecifiedTag(OFFSET_TIME_ORIGINAL_TAG), offsetTime
        );
    }

    private Map<Tag, String> createVideoTags(MediaDateTime mediaDateTime) {
        // For videos, it's a little trickier, because, by convention, the date time is stored in "CreateDate" tag, for example: 2023:08:31 15:10:31
        // However, the tag stores the date by convention in UTC time zone, which is tricky, because then we cannot infer the time zone easily,
        // because videos does not have the "OffsetTime" tag.
        // However, iPhones stores the full offset date and time in "CreationDate" tag, so we can use that, for example: 2023:08:31 18:10:31+03:00
        var utcDateTime = formatToLocalExifDateTime(mediaDateTime.getUtcDateTime().toLocalDateTime());
        var offsetDateTime = formatToOffsetExifDateTime(mediaDateTime.getDateTime());

        return Map.of(
                new UnspecifiedTag(CREATE_DATE_UTC_TAG), utcDateTime,
                new UnspecifiedTag(CREATION_DATE_TAG), offsetDateTime
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

    @Override
    public void close() throws Exception {
        exifTool.close();
    }
}
