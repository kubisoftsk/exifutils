package sk.kubisoft.exifutils.core.analysis;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public record ExifDateTime(

        LocalDateTime localDateTime,

        ZoneOffset zoneOffset

) {

    public ExifDateTime(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        this.localDateTime = Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        this.zoneOffset = zoneOffset;
    }

    @Override
    public String toString() {
        if (zoneOffset == null) {
            return localDateTime.toString();
        } else {
            return localDateTime + " " + zoneOffset;
        }
    }
}
