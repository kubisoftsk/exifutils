package sk.kubisoft.exifutils.core.media;

import sk.kubisoft.exifutils.core.utils.DateTimeUtils;

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

    public MediaDateTime(OffsetDateTime offsetDateTime) {
        this.localDateTime = offsetDateTime.toLocalDateTime();
        this.zoneOffset = offsetDateTime.getOffset();
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

    public LocalDateTime toUTCDateTime() {
        return toOffsetDateTime().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    @Override
    public String toString() {
        if (zoneOffset == null) {
            return DateTimeUtils.formatLocalDateTime(localDateTime);
        } else {
            var offsetFormatted = DateTimeUtils.formatOffsetDateTime(toOffsetDateTime());
            var utcFormatted = DateTimeUtils.formatLocalDateTime(toUTCDateTime());
            return String.format("%s (%s UTC)", offsetFormatted, utcFormatted);
        }
    }
}
