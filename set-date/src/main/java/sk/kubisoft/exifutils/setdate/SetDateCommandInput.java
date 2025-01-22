package sk.kubisoft.exifutils.setdate;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record SetDateCommandInput(

        List<Path> sourcePaths,

        String pattern,

        LocalDateTime dateTime,

        ZoneOffset zoneOffset) {
}
