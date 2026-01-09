package sk.kubisoft.exifutils.setdate;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record SetDateCommandInput(

        String[] inputPaths,

        String pattern,

        LocalDateTime localDateTime,

        ZoneId zoneId,

        boolean rename,

        boolean unknownOnly,

        boolean fixZone) {
}
