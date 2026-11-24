package sk.kubisoft.exifutils.shiftdate;

import sk.kubisoft.exifutils.core.file.FileFilter;
import sk.kubisoft.exifutils.core.file.FileSortOrder;

import java.time.Duration;

public record ShiftDateCommandInput(

        String[] inputPaths,

        Duration duration,

        boolean rename,

        String forceField,

        FileSortOrder sortOrder,

        FileFilter filter
) {

}
