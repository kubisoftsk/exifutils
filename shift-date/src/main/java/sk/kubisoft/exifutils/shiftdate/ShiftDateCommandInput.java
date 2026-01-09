package sk.kubisoft.exifutils.shiftdate;

import java.time.Duration;

public record ShiftDateCommandInput(

        String[] inputPaths,

        Duration duration,

        boolean rename,

        String forceField
) {

}
