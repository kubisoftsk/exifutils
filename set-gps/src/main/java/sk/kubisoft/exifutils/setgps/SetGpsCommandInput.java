package sk.kubisoft.exifutils.setgps;

import java.nio.file.Path;
import java.util.List;

public record SetGpsCommandInput(

        List<Path> sourcePaths,

        Double latitude,

        Double longitude,

        boolean remove

) {
}
