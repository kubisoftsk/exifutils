package sk.kubisoft.exifutils.sort;

import java.nio.file.Path;
import java.util.List;

public record SortCommandInput(

        List<Path> sourceDirectories,

        Path destinationDirectory,

        boolean rename,

        boolean dryRun,

        boolean verbose

) {
}
