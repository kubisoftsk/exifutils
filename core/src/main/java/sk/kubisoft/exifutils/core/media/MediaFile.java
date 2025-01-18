package sk.kubisoft.exifutils.core.media;

import java.nio.file.Path;
import java.util.Map;

public record MediaFile(

        Path originalPath,

        MediaType mediaType,

        Map<String, String> metadata

) {
}
