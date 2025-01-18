package sk.kubisoft.exifutils.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class SortCommand {

    private static final Logger logger = LoggerFactory.getLogger(SortCommand.class);

    private final FileExplorer fileExplorer;
    private final MediaDateExtractor mediaDateExtractor;
    private final MediaFileSorter mediaFileSorter;
    private final FileMover fileMover;

    @Inject
    public SortCommand(FileExplorer fileExplorer, MediaDateExtractor mediaDateExtractor,
                       MediaFileSorter mediaFileSorter, FileMover fileMover) {
        this.fileExplorer = fileExplorer;
        this.mediaDateExtractor = mediaDateExtractor;
        this.mediaFileSorter = mediaFileSorter;
        this.fileMover = fileMover;
    }

    public void execute(SortCommandInput input) {
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

        Map<Path, Path> moveActions = mediaFileSorter.sort(mediaFilesWithDate, input.destinationDirectory());
        logger.info("Files should be sorted as follows:");
        moveActions.forEach((source, target) -> logger.info("Move {} to {}", source, target));

        if (input.dryRun()) {
            logger.info("Dry run, not moving files");
        } else {
            logger.info("Moving files...");
            fileMover.moveFiles(moveActions);
        }
    }

}
