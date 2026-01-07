package sk.kubisoft.exifutils.sort;

import java.nio.file.Path;

public record SortCommandInput(

        String[] inputPaths,

        Path outputDir,

        boolean rename,

        boolean writeDate,

        /**
         * Custom sort pattern using ${date,FORMAT} syntax.
         * If null, the default pattern from configuration is used.
         */
        String sortPattern

) {
}
