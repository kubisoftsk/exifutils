package sk.kubisoft.exifutils.rename;

import java.nio.file.Path;
import java.time.ZoneId;

public record RenameCommandInput(

        String[] inputPaths,

        Path outputDir,

        boolean writeDate,

        ZoneId zoneId,

        String forceField

) {
}
