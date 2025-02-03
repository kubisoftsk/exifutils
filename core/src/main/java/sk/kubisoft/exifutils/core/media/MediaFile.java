package sk.kubisoft.exifutils.core.media;

import java.nio.file.Path;

public record MediaFile(

        Path originalPath,

        MediaType mediaType,

        MediaDateTime creationDate

) {
}
