package sk.kubisoft.exifutils.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RenameCommand {

    private static final Logger logger = LoggerFactory.getLogger(RenameCommand.class);

    private final FileExplorer fileExplorer;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaFileNameUtils fileNameUtils;
    private final FileMover fileMover;
    private final Console console;

    @Inject
    public RenameCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                         MediaFileNameUtils fileNameUtils, FileMover fileMover, Console console) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.fileNameUtils = fileNameUtils;
        this.fileMover = fileMover;
        this.console = console;
    }

    public void execute(RenameCommandInput input) {
        console.verboseln("Running ExifUtils Rename command with input: %s", input);

        console.println("Searching for media files...");
        var allFiles = fileExplorer.listFiles(input.sourceDirectories());
        console.println("Found %d files.", allFiles.size());

        List<MediaFile> mediaFilesWithDate = mediaAnalyzer.analyze(allFiles);

        List<MoveAction> moveActions = createMoveActions(mediaFilesWithDate);
        console.println("Total %d files will be renamed:", moveActions.size());
        moveActions.forEach((action) -> console.println("Rename %s", action));

        // Confirm action or abort
        if (console.confirmAction("Do you want to continue?")) {
            console.println("Renaming files...");
            fileMover.moveFiles(moveActions);

        } else {
            console.println("Aborted.");
        }
    }

    private List<MoveAction> createMoveActions(List<MediaFile> mediaFiles) {
        List<MoveAction> moveActions = new ArrayList<>();

        for (var mediaFile : mediaFiles) {
            var originalPath = mediaFile.originalPath();

            var newName = fileNameUtils.createNewName(mediaFile, mediaFile.creationDate());

            var targetPath = originalPath.getParent().resolve(newName);
            if (originalPath.equals(targetPath)) {
                logger.debug("Skipping rename action, source and target are the same: {}", originalPath);
                continue;
            }
            logger.debug("Created move action {} to {}", originalPath, targetPath);
            moveActions.add(new MoveAction(originalPath, targetPath));
        }

        return moveActions;
    }
}