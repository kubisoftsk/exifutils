package sk.kubisoft.exifutils.sort;

import java.nio.file.Path;

public record SortCommandInput(

        String[] inputPaths,

        Path outputDir,

        boolean rename,

        boolean writeDate

) {
}
