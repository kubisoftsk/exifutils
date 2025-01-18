package sk.kubisoft.exifutils.core.media;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class MediaDateTime {

    private final LocalDateTime localDateTime;

    private final ZoneOffset zoneOffset;

    public MediaDateTime(LocalDateTime localDateTime) {
        this(localDateTime, null);
    }

    public MediaDateTime(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        this.localDateTime = Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        this.zoneOffset = zoneOffset;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public boolean hasZoneOffset() {
        return zoneOffset != null;
    }

    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    public OffsetDateTime toOffsetDateTime() {
        if (zoneOffset == null) {
            throw new IllegalStateException("ZoneOffset is not set");
        }
        return localDateTime.atOffset(zoneOffset);
    }

    @Override
    public String toString() {
        return "MediaDateTime{" +
                "localDateTime=" + localDateTime +
                ", zoneOffset=" + zoneOffset +
                '}';
    }
}
