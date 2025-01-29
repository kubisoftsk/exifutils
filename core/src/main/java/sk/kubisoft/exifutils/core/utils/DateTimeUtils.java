package sk.kubisoft.exifutils.core.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter LOCAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX");


    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(LOCAL_FORMATTER);
    }

    public static String formatOffsetDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.format(OFFSET_FORMATTER);
    }

}
