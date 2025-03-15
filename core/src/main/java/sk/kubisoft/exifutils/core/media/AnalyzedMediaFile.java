package sk.kubisoft.exifutils.core.media;

import java.nio.file.Path;
import java.util.Map;

public class AnalyzedMediaFile extends MediaFile {

    private final Map<String, String> metadata;

    private final MediaDateTime creationDate;

    public AnalyzedMediaFile(Path originalPath, MediaType mediaType, Map<String, String> metadata) {
        this(originalPath, mediaType, metadata, null);
    }

    public AnalyzedMediaFile(Path originalPath, MediaType mediaType, Map<String, String> metadata, MediaDateTime creationDate) {
        super(originalPath, mediaType);
        this.metadata = metadata;
        this.creationDate = creationDate;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public MediaDateTime getCreationDate() {
        return creationDate;
    }

    @Override
    public String toString() {
        return "AnalyzedMediaFile{" +
                "path='" + getOriginalPath() + '\'' +
                ", mediaType=" + getMediaType() +
                ", metadata=" + metadata +
                ", creationDate=" + creationDate +
                '}';
    }
}
