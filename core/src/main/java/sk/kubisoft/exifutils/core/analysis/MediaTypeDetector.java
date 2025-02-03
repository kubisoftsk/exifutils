package sk.kubisoft.exifutils.core.analysis;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class MediaTypeDetector {

    private static final List<String> COMMON_VIDEO_EXTENSIONS = List.of("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "vob", "m4v", "3gp", "3g2", "mpg", "mpeg", "m2v", "m4v", "ts", "mts", "m2ts", "asf", "rm", "rmvb", "ogv", "ogg", "drc", "dat", "m2p", "m2ts", "k3g", "skm", "evo", "nsv", "pva", "tp", "tpr", "ts", "trp", "m2t", "m2ts", "mts");
    private static final List<String> COMMON_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "heic", "heif");

    private final Tika tika;

    public MediaTypeDetector(Tika tika) {
        this.tika = tika;
    }

    MediaType detectMediaType(Path path) {
        String extension = StringUtils.toRootLowerCase(FilenameUtils.getExtension(path.toString()));
        if (COMMON_VIDEO_EXTENSIONS.contains(extension)) {
            return MediaType.VIDEO;
        }
        if (COMMON_IMAGE_EXTENSIONS.contains(extension)) {
            return MediaType.IMAGE;
        }

        try {
            String mimeType = tika.detect(path);

            if (mimeType.startsWith("video")) {
                return MediaType.VIDEO;
            } else if (mimeType.startsWith("image")) {
                return MediaType.IMAGE;
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new AnalysisException(path, "Error detecting media type", e);
        }
    }
}
