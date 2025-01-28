package sk.kubisoft.exifutils.rename;

import java.nio.file.Path;
import java.util.List;

public record RenameCommandInput(

        List<Path> sourceDirectories,

        boolean writeDate

) {
}
