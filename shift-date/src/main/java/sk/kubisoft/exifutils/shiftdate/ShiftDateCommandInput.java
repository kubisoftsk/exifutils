package sk.kubisoft.exifutils.shiftdate;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public record ShiftDateCommandInput(

        List<Path> sourcePaths,

        Duration duration,

        boolean rename
) {

}
