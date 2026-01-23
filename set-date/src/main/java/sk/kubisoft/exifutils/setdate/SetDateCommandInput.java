package sk.kubisoft.exifutils.setdate;

import sk.kubisoft.exifutils.core.file.FileSortOrder;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record SetDateCommandInput(

        String[] inputPaths,

        String pattern,

        LocalDateTime localDateTime,

        ZoneId zoneId,

        boolean rename,

        boolean unknownOnly,

        boolean fixZone,

        String forceField,

        FileSortOrder sortOrder) {
}
