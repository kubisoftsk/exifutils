package sk.kubisoft.exifutils.core.metadata;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.ExifToolOptions;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardOptions;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
import org.slf4j.Logger;
import sk.kubisoft.exifutils.core.analysis.AnalysisException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MetaDataHandler implements AutoCloseable {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MetaDataHandler.class);

    private final ExifTool exifTool;

    private final ExifToolOptions exifToolOptions = StandardOptions.builder()
            .withOverwriteOriginal()
            .build();

    MetaDataHandler(String exifToolPath) {
        this.exifTool = new ExifToolBuilder()
                .withPath(exifToolPath)
                .enableStayOpen()  // Performance optimization for multiple files
                .build();
    }

    public Map<String, String> extractMetaData(Path file) {
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

    public void setMetaDataTags(Path file, Map<String, String> newTags) {
        try {
            var tags = convertStringsToTags(newTags);
            logger.info("Setting metadata tags: {}", newTags);
            exifTool.setImageMeta(file.toFile(), exifToolOptions, tags);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<Tag, String> convertStringsToTags(Map<String, String> newTags) {
        return newTags.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(new UnspecifiedTag(entry.getKey()), entry.getValue()), Map::putAll);
    }

    @Override
    public void close() throws Exception {
        exifTool.close();
    }
}
