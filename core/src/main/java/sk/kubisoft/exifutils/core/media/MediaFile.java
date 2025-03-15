package sk.kubisoft.exifutils.core.media;

import java.nio.file.Path;
import java.util.Objects;

public class MediaFile {

    private final Path originalPath;

    private final MediaType mediaType;

    public MediaFile(Path originalPath, MediaType mediaType) {
        this.originalPath = Objects.requireNonNull(originalPath, "Path is required");
        this.mediaType = Objects.requireNonNull(mediaType, "MediaType is required");
    }

    public Path getOriginalPath() {
        return originalPath;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MediaFile mediaFile = (MediaFile) o;
        return Objects.equals(originalPath, mediaFile.originalPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(originalPath);
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "originalPath=" + originalPath +
                ", mediaType=" + mediaType +
                '}';
    }
}
