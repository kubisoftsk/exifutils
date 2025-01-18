package sk.kubisoft.exifutils.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Map;

@Singleton
public class SortCommand {

    private static final Logger logger = LoggerFactory.getLogger(SortCommand.class);

    private final FileExplorer fileExplorer;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaFileSorter mediaFileSorter;
    private final FileMover fileMover;

    @Inject
    public SortCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                       MediaFileSorter mediaFileSorter, FileMover fileMover) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.mediaFileSorter = mediaFileSorter;
        this.fileMover = fileMover;
    }

    public void execute(SortCommandInput input) {
        logger.info("Running ExifUtils Sort command with input: {}", input);

        var allFiles = fileExplorer.listFiles(input.sourceDirectories());
        logger.info("Found {} files", allFiles.size());

        Map<MediaFile, MediaDateTime> mediaFilesWithDate = mediaAnalyzer.analyzeCreationDate(allFiles);

        logger.info("Found {} media files with valid dates", mediaFilesWithDate.size());

        Map<Path, Path> moveActions = mediaFileSorter.sort(mediaFilesWithDate, input.destinationDirectory(), input.rename());
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
