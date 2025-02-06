package sk.kubisoft.exifutils.core.media;

import sk.kubisoft.exifutils.core.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class MediaDateTime {

    private final OffsetDateTime utcDateTime;

    private final LocalDateTime localDateTime;

    private final ZoneOffset zoneOffset;

    public MediaDateTime(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        this.localDateTime = Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        this.zoneOffset = Objects.requireNonNull(zoneOffset, "zoneOffset must not be null");
        this.utcDateTime = OffsetDateTime.of(localDateTime, zoneOffset).withOffsetSameInstant(ZoneOffset.UTC);
    }

    public OffsetDateTime getUtcDateTime() {
        return utcDateTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    public OffsetDateTime getDateTime() {
        return OffsetDateTime.of(localDateTime, zoneOffset);
    }

    @Override
    public String toString() {
        var offsetFormatted = DateTimeUtils.formatOffsetDateTime(getDateTime());
        var utcFormatted = DateTimeUtils.formatLocalDateTime(this.utcDateTime.toLocalDateTime());
        return String.format("%s (%s UTC)", offsetFormatted, utcFormatted);

    }
}
