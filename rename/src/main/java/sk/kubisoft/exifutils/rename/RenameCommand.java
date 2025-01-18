package sk.kubisoft.exifutils.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Map;

@Singleton
public class RenameCommand {

    private static final Logger logger = LoggerFactory.getLogger(RenameCommand.class);

    private final FileExplorer fileExplorer;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaFileNameUtils fileNameUtils;
    private final FileMover fileMover;

    @Inject
    public RenameCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                         MediaFileNameUtils fileNameUtils, FileMover fileMover) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.fileNameUtils = fileNameUtils;
        this.fileMover = fileMover;
    }

    public void execute(RenameCommandInput input) {
        logger.info("Running ExifUtils Rename command with input: {}", input);

        var allFiles = fileExplorer.listFiles(input.sourceDirectories());
        logger.info("Found {} files", allFiles.size());

        Map<MediaFile, MediaDateTime> mediaFilesWithDate = mediaAnalyzer.analyzeCreationDate(allFiles);

        logger.info("Found {} media files with valid dates", mediaFilesWithDate.size());

        Map<Path, Path> moveActions = createMoveActions(mediaFilesWithDate);
        logger.info("Files should be renamed as follows:");
        moveActions.forEach((source, target) -> logger.info("Move {} to {}", source, target));

        if (input.dryRun()) {
            logger.info("Dry run, not moving files");
        } else {
            logger.info("Moving files...");
            fileMover.moveFiles(moveActions);
        }
    }

    private Map<Path, Path> createMoveActions(Map<MediaFile, MediaDateTime> mediaFilesWithDate) {
        Map<Path, Path> moveActions = new java.util.LinkedHashMap<>();

        for (var entry : mediaFilesWithDate.entrySet()) {
            var mediaFile = entry.getKey();
            var originalPath = mediaFile.originalPath();
            var date = entry.getValue();

            var newName = fileNameUtils.createNewName(mediaFile, date);

            var targetPath = originalPath.getParent().resolve(newName);
            moveActions.put(originalPath, targetPath);
        }

        return moveActions;
    }
}