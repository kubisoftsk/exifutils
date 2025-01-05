package sk.kubisoft.exifsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifsort.config.MediaFileSorter;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExifSort {

    private static final Logger logger = LoggerFactory.getLogger(ExifSort.class);

    private final FileExplorer fileExplorer = new FileExplorer();

    private final MediaDateExtractor mediaDateExtractor = new MediaDateExtractor();

    public void run(ExifSortApplicationInput input) {
        logger.info("Running ExifSort with input: {}", input);

        var allFiles = fileExplorer.listFiles(input.sourceDirectories());
        logger.info("Found {} files", allFiles.size());

        Map<MediaFile, MediaDateTime> mediaFilesWithDate = new LinkedHashMap<>();

        try (var metaDataExtractor = new MetaDataExtractor()) {
            for (int i = 0; i < allFiles.size(); i++) {
                var file = allFiles.get(i);
                logger.info("Processing file {} of {}: {}", i + 1, allFiles.size(), file);

                try {
                    var mediaFileOptional = metaDataExtractor.extractMetaData(file);
                    if (mediaFileOptional.isEmpty()) {
                        logger.info("No metadata found, skipping file: {}", file);
                        continue;
                    }

                    var mediaFile = mediaFileOptional.get();
                    var dateOptional = mediaDateExtractor.extractCreationDate(mediaFile);
                    if (dateOptional.isPresent()) {
                        var date = dateOptional.get();
                        logger.info("Found creation date: {}", date);
                        mediaFilesWithDate.put(mediaFile, date);
                    } else {
                        logger.info("No valid date found");
                    }
                } catch (Exception e) {
                    logger.error("Error processing file: {}", file, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }

        logger.info("Found {} files with valid dates", mediaFilesWithDate.size());

        Map<Path, Path> moveActions = new MediaFileSorter().sort(mediaFilesWithDate, input.destinationDirectory());
        logger.info("Files should be sorted as follows:");
        moveActions.forEach((source, target) -> logger.info("Move {} to {}", source, target));

        if (input.dryRun()) {
            logger.info("Dry run, not moving files");
        } else {
            logger.info("Moving files...");
            new FileMover().moveFiles(moveActions);
        }
    }

}
