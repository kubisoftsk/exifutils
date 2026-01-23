package sk.kubisoft.exifutils.sort;

import sk.kubisoft.exifutils.core.file.FileSortOrder;

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
        String sortPattern,

        /**
         * If true, copy files instead of moving them.
         * Useful for experimenting with sort patterns.
         */
        boolean copy,

        /**
         * If set, force date extraction from the specified EXIF field,
         * bypassing device profiles.
         */
        String forceField,

        FileSortOrder sortOrder

) {
}
