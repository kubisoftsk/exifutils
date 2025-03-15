package sk.kubisoft.exifutils.setdate;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record SetDateCommandInput(

        String[] sourcePaths,

        String pattern,

        LocalDateTime localDateTime,

        ZoneId zoneId,

        boolean rename,

        boolean unknownOnly) {
}
