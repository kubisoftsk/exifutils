package sk.kubisoft.exifutils.core.metadata;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.ExifToolOptions;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardOptions;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MetaDataSetter implements AutoCloseable {

    private static final String DATE_TIME_TAG = "DateTimeOriginal";
    private static final String OFFSET_TIME_TAG = "OffsetTimeOriginal";

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

    public void setDateTime(Path file, LocalDateTime dateTime, ZoneOffset zoneOffset) {
        Map<Tag, String> newTags = new HashMap<>();
        newTags.put(new UnspecifiedTag(DATE_TIME_TAG), formatToExifDateTime(dateTime));
        newTags.put(new UnspecifiedTag(OFFSET_TIME_TAG), zoneOffset.toString());

        try {
            exifTool.setImageMeta(file.toFile(), exifToolOptions, newTags);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String formatToExifDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    @Override
    public void close() throws Exception {
        exifTool.close();
    }
}
