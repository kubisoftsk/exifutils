package sk.kubisoft.exifutils.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;

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
    private final DuplicatePreProcessor duplicatePreProcessor;
    private final FileMover fileMover;
    private final Console console;
    private final ExifDateSetter exifDateSetter;

    @Inject
    public RenameCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer, MediaFileNameUtils fileNameUtils,
                         DuplicatePreProcessor duplicatePreProcessor, FileMover fileMover, Console console, ExifDateSetter exifDateSetter) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.fileNameUtils = fileNameUtils;
        this.duplicatePreProcessor = duplicatePreProcessor;
        this.fileMover = fileMover;
        this.console = console;
        this.exifDateSetter = exifDateSetter;
    }

    public void execute(RenameCommandInput input) {
        console.verboseln("Running ExifUtils Rename command with input: %s", input);

        console.println("Searching for media files...");
        var allFiles = fileExplorer.listFiles(input.sourceDirectories());

        var allMediaFiles = mediaAnalyzer.analyze(allFiles);
        List<MediaFile> mediaFilesWithDate = allMediaFiles.stream()
                .filter(mediaFile -> mediaFile.creationDate() != null)
                .toList();
        List<MediaFile> mediaFilesWithoutDate = allMediaFiles.stream()
                .filter(mediaFile -> mediaFile.creationDate() == null)
                .toList();

        console.println("Found %d media files with date, %d media files without date.", mediaFilesWithDate.size(), mediaFilesWithoutDate.size());
        mediaFilesWithoutDate.forEach((mediaFile) -> console.verboseln("No date found for %s", mediaFile.originalPath()));

        if (input.writeDate()) {
            var setDateActions = mediaFilesWithDate.stream()
                    .filter(exifDateSetter::needsDateTimeSet)
                    .map(mediaFile -> new SetDateAction(mediaFile.originalPath(), mediaFile.mediaType(), mediaFile.creationDate()))
                    .toList();

            if (setDateActions.isEmpty()) {
                console.println("No files to set date.");
            } else {
                console.println("Total %d files will have date set:", setDateActions.size());
                setDateActions.forEach((action) -> console.println("%s", action));
                if (console.confirmAction("Do you want to continue?")) {
                    exifDateSetter.setDateTime(setDateActions);
                } else {
                    console.println("Aborted.");
                }
            }
        }

        List<MoveAction> moveActions = createMoveActions(mediaFilesWithDate);

        if (moveActions.isEmpty()) {
            console.println("No files to rename.");
            return;
        }
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
        List<MoveAction> rawMoveActions = new ArrayList<>();

        for (var mediaFile : mediaFiles) {
            var originalPath = mediaFile.originalPath();

            var newName = fileNameUtils.createNewName(mediaFile, mediaFile.creationDate());
            var targetPath = originalPath.getParent().resolve(newName);

            rawMoveActions.add(new MoveAction(originalPath, targetPath));
        }

        rawMoveActions.sort(MoveAction::compareTo);
        var moveActions = duplicatePreProcessor.processConflicts(rawMoveActions);

        return filterOutUnchangedActions(moveActions);
    }

    private List<MoveAction> filterOutUnchangedActions(List<MoveAction> moveActions) {
        return moveActions.stream()
                .filter(action -> !action.source().equals(action.target()))
                .peek(action -> logger.debug("Created move action {}", action))
                .toList();
    }
}