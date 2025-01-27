package sk.kubisoft.exifutils.info;

import java.nio.file.Path;
import java.util.List;

public record InfoCommandInput(

        List<Path> paths,

        boolean extractDate

) {
}
