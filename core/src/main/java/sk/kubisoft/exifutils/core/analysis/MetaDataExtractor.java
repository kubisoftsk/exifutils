package sk.kubisoft.exifutils.core.analysis;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

class MetaDataExtractor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MetaDataExtractor.class);

    private final ExifTool exifTool;

    MetaDataExtractor(String exifToolPath) {
        this.exifTool = new ExifToolBuilder()
                .withPath(exifToolPath)
                .enableStayOpen()  // Performance optimization for multiple files
                .build();
    }

    Map<String, String> extractMetaData(Path file) {
        try {
            var metadata = exifTool.getImageMeta(file.toFile());
            return convertMetadataToStrings(metadata);
        } catch (Exception e) {
            throw new AnalysisException(file, "Error extracting metadata", e);
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
