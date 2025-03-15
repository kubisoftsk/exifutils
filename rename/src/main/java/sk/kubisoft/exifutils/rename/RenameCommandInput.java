package sk.kubisoft.exifutils.rename;

import java.time.ZoneId;

public record RenameCommandInput(

        String[] inputPaths,

        boolean writeDate,

        ZoneId zoneId

) {
}
