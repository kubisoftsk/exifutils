package sk.kubisoft.exifutils.core.media;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public record MediaFile(

        Path originalPath,

        MediaType mediaType,

        Map<String, String> metadata,

        MediaDateTime creationDate

) {

    public MediaFile(Path originalPath, MediaType mediaType) {
        this(originalPath, mediaType, Collections.emptyMap(), null);
    }

    public MediaFile(Path originalPath, MediaType mediaType, Map<String, String> metadata) {
        this(originalPath, mediaType, metadata, null);
    }

    public MediaFile(Path originalPath, MediaType mediaType, Map<String, String> metadata, MediaDateTime creationDate) {
        this.originalPath = originalPath;
        this.mediaType = mediaType;
        this.metadata = metadata;
        this.creationDate = creationDate;
    }
}
