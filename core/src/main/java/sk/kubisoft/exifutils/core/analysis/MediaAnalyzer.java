package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class MediaAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(MediaAnalyzer.class);

    private final ConfigService configService;
    private final MediaDateExtractor mediaDateExtractor;

    @Inject
    public MediaAnalyzer(ConfigService configService, MediaDateExtractor mediaDateExtractor) {
        this.configService = configService;
        this.mediaDateExtractor = mediaDateExtractor;
    }

    public Map<MediaFile, MediaDateTime> analyzeCreationDate(List<Path> files) {
        Map<MediaFile, MediaDateTime> mediaFilesWithDate = new LinkedHashMap<>();

        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured");
        }
        try (var metaDataExtractor = new MetaDataExtractor(exifToolConfig.getPath())) {
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);
                logger.info("Analyzing file {} of {}: {}", i + 1, files.size(), file);

                try {
                    var mediaFileOptional = metaDataExtractor.extractMetaData(file);
                    if (mediaFileOptional.isEmpty()) {
                        logger.debug("No metadata found, skipping file: {}", file);
                        continue;
                    }

                    var mediaFile = mediaFileOptional.get();
                    var dateOptional = mediaDateExtractor.extractCreationDate(mediaFile);
                    if (dateOptional.isPresent()) {
                        var date = dateOptional.get();
                        logger.debug("Found creation date: {}", date);
                        mediaFilesWithDate.put(mediaFile, date);
                    } else {
                        logger.debug("No valid date found");
                    }
                } catch (Exception e) {
                    logger.error("Error processing file: {}", file, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }

        return mediaFilesWithDate;
    }
}
