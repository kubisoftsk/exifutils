package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.metadata.MetaDataExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class MediaAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(MediaAnalyzer.class);

    private final Console console;
    private final ConfigService configService;
    private final MediaDateExtractor mediaDateExtractor;

    @Inject
    public MediaAnalyzer(Console console, ConfigService configService,
                         MediaDateExtractor mediaDateExtractor) {
        this.console = console;
        this.configService = configService;
        this.mediaDateExtractor = mediaDateExtractor;
    }

    public List<MediaFile> getMetaData(List<Path> files) {
        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured");
        }
        List<MediaFile> mediaFiles = new ArrayList<>();
        try (var metaDataExtractor = new MetaDataExtractor(exifToolConfig.getPath())) {
            console.println("Starting analysis of media files...", files.size());
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);
                if (console.isVerbose()) {
                    console.println("Analyzing file %d of %d: %s", i + 1, files.size(), file);
                } else {
                    console.progress("Analyzing file %d of %d: %s", i + 1, files.size(), file);
                }

                try {
                    var mediaFileOptional = metaDataExtractor.extractMetaData(file);
                    if (mediaFileOptional.isEmpty()) {
                        console.verbose("No metadata found, skipping file");
                        continue;
                    }

                    mediaFiles.add(mediaFileOptional.get());
                } catch (Exception e) {
                    console.error("Error processing file: %s", e, file);
                }
            }
            if (!console.isVerbose()) {
                console.progress(""); // Clear progress line
            }
            console.println("Analysis finished.");
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }
        return mediaFiles;
    }

    public Map<MediaFile, MediaDateTime> analyzeCreationDate(List<Path> files) {
        Map<MediaFile, MediaDateTime> mediaFilesWithDate = new LinkedHashMap<>();

        List<MediaFile> mediaFiles = getMetaData(files);
        for (var mediaFile : mediaFiles) {
            var dateOptional = mediaDateExtractor.extractCreationDate(mediaFile);
            if (dateOptional.isPresent()) {
                var date = dateOptional.get();
                console.verbose("Found creation date: %s", date);
                mediaFilesWithDate.put(mediaFile, date);
            } else {
                console.verbose("No valid date found");
            }
        }
        return mediaFilesWithDate;
    }

}
