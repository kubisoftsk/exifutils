package sk.kubisoft.exifutils.rename;

import sk.kubisoft.exifutils.core.file.FileSortOrder;

import java.nio.file.Path;
import java.time.ZoneId;

public record RenameCommandInput(

        String[] inputPaths,

        Path outputDir,

        boolean writeDate,

        ZoneId zoneId,

        String forceField,

        FileSortOrder sortOrder

) {
}
