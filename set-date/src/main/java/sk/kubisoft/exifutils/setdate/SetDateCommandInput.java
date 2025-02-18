package sk.kubisoft.exifutils.setdate;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public record SetDateCommandInput(

        List<Path> sourcePaths,

        String pattern,

        LocalDateTime localDateTime,

        ZoneId zoneId,

        boolean rename,

        boolean unknownOnly) {
}
