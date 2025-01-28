package sk.kubisoft.exifutils.setdate;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

public record SetDateCommandInput(

        List<Path> sourcePaths,

        String pattern,

        OffsetDateTime dateTime,

        boolean writeDate) {
}
