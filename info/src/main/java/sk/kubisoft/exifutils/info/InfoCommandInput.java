package sk.kubisoft.exifutils.info;

import sk.kubisoft.exifutils.core.file.FileFilter;

public record InfoCommandInput(

        String[] inputPaths,

		boolean printAll,

        FileFilter filter

) {
}
