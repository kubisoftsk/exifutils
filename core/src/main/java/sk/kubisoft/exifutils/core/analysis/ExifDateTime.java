package sk.kubisoft.exifutils.core.analysis;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public record ExifDateTime(

        LocalDateTime localDateTime,

		boolean localTime,

        ZoneOffset zoneOffset

) {

    public ExifDateTime(LocalDateTime localDateTime, boolean localTime, ZoneOffset zoneOffset) {
        this.localDateTime = Objects.requireNonNull(localDateTime, "localDateTime must not be null");
		this.localTime = localTime;
        this.zoneOffset = zoneOffset;
    }

    @Override
    public String toString() {
        if (zoneOffset == null) {
            return localDateTime + " " + (localTime ? "local" : "UTC");
        } else {
            return localDateTime + " " + zoneOffset + " " + (localTime ? "local" : "UTC");
        }
    }
}
