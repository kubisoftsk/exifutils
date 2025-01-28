package sk.kubisoft.exifutils.core.metadata;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.media.MediaFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class MetaDataExtractor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MetaDataExtractor.class);

    private final MediaTypeDetector mediaTypeDetector;

    private final ExifTool exifTool;

    public MetaDataExtractor(String exifToolPath, MediaTypeDetector mediaTypeDetector) {
        this.mediaTypeDetector = mediaTypeDetector;
        this.exifTool = new ExifToolBuilder()
                .withPath(exifToolPath)
                .enableStayOpen()  // Performance optimization for multiple files
                .build();
    }

    public Optional<MediaFile> extractMetaData(Path file) {
        try {
            var mediaType = mediaTypeDetector.getMediaType(file);
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

    @Override
    public void close() throws Exception {
        exifTool.close();
    }
}
