package sk.kubisoft.exifutils.setgps;

import sk.kubisoft.exifutils.core.file.FileFilter;

public record SetGpsCommandInput(

        String[] inputPaths,

        Double latitude,

        Double longitude,

        boolean remove,

        FileFilter filter

) {
}
