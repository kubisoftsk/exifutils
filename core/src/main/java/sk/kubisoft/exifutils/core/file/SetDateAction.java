package sk.kubisoft.exifutils.core.file;

import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaType;
import sk.kubisoft.exifutils.core.utils.DateTimeUtils;

import java.nio.file.Path;

public record SetDateAction(

        Path file,

        MediaType mediaType,

        MediaDateTime dateTime
) {

    public SetDateAction {
        if (file == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Date time cannot be null.");
        }
    }

    @Override
    public String toString() {
        var formattedDateTime = DateTimeUtils.formatLocalDateTime(dateTime.getLocalDateTime());
        return String.format("Set date to file %s to %s %s", file, formattedDateTime, dateTime.getZoneOffset());
    }
}
