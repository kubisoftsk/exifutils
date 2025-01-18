package sk.kubisoft.exifutils.sort;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class MetaDataExtractor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MetaDataExtractor.class);

    private static final List<String> COMMON_VIDEO_EXTENSIONS = List.of("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "vob", "m4v", "3gp", "3g2", "mpg", "mpeg", "m2v", "m4v", "ts", "mts", "m2ts", "asf", "rm", "rmvb", "ogv", "ogg", "drc", "dat", "m2p", "m2ts", "k3g", "skm", "evo", "nsv", "pva", "tp", "tpr", "ts", "trp", "m2t", "m2ts", "mts");
    private static final List<String> COMMON_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "heic", "heif");

    private final Tika tika = new Tika();
    private final ConfigService configService = ConfigService.getInstance();

    private final ExifTool exifTool;

    public MetaDataExtractor() {
        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured");
        }

        this.exifTool = new ExifToolBuilder()
                .withPath(exifToolConfig.getPath())
                .enableStayOpen()  // Performance optimization for multiple files
                .build();
    }

    public Optional<MediaFile> extractMetaData(Path file) {
        try {
            var mediaType = getMediaType(file);
            if (mediaType == null) {
                return Optional.empty();
            }
            var metadata = exifTool.getImageMeta(file.toFile());
            Map<String, String> metaDataStrings = convertMetadataToStrings(metadata);
            return Optional.of(new MediaFile(file, mediaType, metaDataStrings));
        } catch (Exception e) {
            logger.error("Error extracting metadata from file: {}", file, e);
            return Optional.empty();
        }
    }

    private Map<String, String> convertMetadataToStrings(Map<Tag, String> metadata) {
        Map<String, String> metaDataStrings = new TreeMap<>();

        metadata.forEach((key1, value) -> {
            String key = key1.getName();
            metaDataStrings.put(key, value);
        });

        return metaDataStrings;
    }

    private MediaType getMediaType(Path path) {
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
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws Exception {
        exifTool.close();
    }
}
