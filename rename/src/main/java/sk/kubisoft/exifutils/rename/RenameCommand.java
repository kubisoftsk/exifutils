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
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;
import sk.kubisoft.exifutils.core.utils.DateTimeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RenameCommand {

    private static final Logger logger = LoggerFactory.getLogger(RenameCommand.class);

    private final FileExplorer fileExplorer;
    private final MediaFileNameUtils fileNameUtils;
    private final MediaAnalyzer mediaAnalyzer;
    private final DuplicatePreProcessor duplicatePreProcessor;
    private final FileMover fileMover;
    private final Console console;
    private final ExifDateSetter exifDateSetter;

    @Inject
    public RenameCommand(FileExplorer fileExplorer, MediaFileNameUtils fileNameUtils, MediaAnalyzer mediaAnalyzer,
                         DuplicatePreProcessor duplicatePreProcessor, FileMover fileMover, Console console, ExifDateSetter exifDateSetter) {
        this.fileExplorer = fileExplorer;
        this.fileNameUtils = fileNameUtils;
        this.mediaAnalyzer = mediaAnalyzer;
        this.duplicatePreProcessor = duplicatePreProcessor;
        this.fileMover = fileMover;
        this.console = console;
        this.exifDateSetter = exifDateSetter;
    }

    public void execute(RenameCommandInput input) {
        console.verboseln("Running ExifUtils Rename command with input: %s", input);

        console.println("Searching for media files...");
        List<MediaFile> mediaFiles = fileExplorer.listMediaFiles(input.inputPaths());
        console.println("Found %d files.", mediaFiles.size());

        List<AnalyzedMediaFile> analyzedFiles = mediaAnalyzer.analyze(mediaFiles, input.forceField());

        List<AnalyzedMediaFile> mediaFilesWithDate = analyzedFiles.stream()
                .filter(mediaFile -> mediaFile.getCreationDate() != null)
                .toList();
        List<AnalyzedMediaFile> mediaFilesWithoutDate = analyzedFiles.stream()
                .filter(mediaFile -> mediaFile.getCreationDate() == null)
                .toList();

        console.println("Found %d media files with date, %d media files without date.", mediaFilesWithDate.size(), mediaFilesWithoutDate.size());
        mediaFilesWithoutDate.forEach((mediaFile) -> console.println("No date found for %s", mediaFile.getOriginalPath()));

        if (input.writeDate()) {
            var setDateActions = mediaFilesWithDate.stream()
                    .filter(exifDateSetter::needsDateTimeSet)
                    .map(mediaFile -> createSetDateAction(mediaFile, input.zoneId()))
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
                    // Do not continue with renaming if user aborted
                    return;
                }
            }
        }

        List<MoveAction> moveActions = createMoveActions(mediaFilesWithDate, input.outputDir());

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

    private SetDateAction createSetDateAction(AnalyzedMediaFile mediaFile, ZoneId zoneIdToUse) {
        if (zoneIdToUse == null) {
            return new SetDateAction(mediaFile.getOriginalPath(), mediaFile.getMediaType(), mediaFile.getCreationDate());
        } else {
            var originalDate = mediaFile.getCreationDate();
            var zoneOffset = DateTimeUtils.getDefaultZoneOffset(originalDate.getLocalDateTime(), zoneIdToUse);
            MediaDateTime newDate = new MediaDateTime(originalDate.getLocalDateTime(), zoneOffset);

            return new SetDateAction(mediaFile.getOriginalPath(), mediaFile.getMediaType(), newDate);
        }
    }

    private List<MoveAction> createMoveActions(List<AnalyzedMediaFile> mediaFiles, Path outputDir) {
        List<MoveAction> rawMoveActions = new ArrayList<>();

        for (var mediaFile : mediaFiles) {
            var originalPath = mediaFile.getOriginalPath();

            var newName = fileNameUtils.createNewName(mediaFile);
            Path targetPath;
            if (outputDir == null) {
                targetPath = originalPath.getParent().resolve(newName);
            } else {
                targetPath = outputDir.resolve(newName);
            }

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