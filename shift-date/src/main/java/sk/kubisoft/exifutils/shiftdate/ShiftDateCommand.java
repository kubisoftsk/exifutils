package sk.kubisoft.exifutils.shiftdate;

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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class ShiftDateCommand {

    private static final Logger logger = LoggerFactory.getLogger(ShiftDateCommand.class);

    private final Console console;
    private final FileExplorer fileExplorer;
    private final ExifDateSetter exifDateSetter;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaFileNameUtils fileNameUtils;
    private final DuplicatePreProcessor duplicatePreProcessor;
    private final FileMover fileMover;

    @Inject
    public ShiftDateCommand(Console console, FileExplorer fileExplorer, ExifDateSetter exifDateSetter, MediaAnalyzer mediaAnalyzer,
                            MediaFileNameUtils fileNameUtils, DuplicatePreProcessor duplicatePreProcessor, FileMover fileMover) {
        this.console = console;
        this.fileExplorer = fileExplorer;
        this.exifDateSetter = exifDateSetter;
        this.mediaAnalyzer = mediaAnalyzer;
        this.fileNameUtils = fileNameUtils;
        this.duplicatePreProcessor = duplicatePreProcessor;
        this.fileMover = fileMover;
    }

    public void execute(ShiftDateCommandInput input) {
        console.verboseln("Running ExifUtils shift-date command with input: %s", input);

        console.println("Searching for media files...");
        List<MediaFile> allMediaFiles = fileExplorer.listMediaFiles(input.inputPaths());
        console.println("Found %d files.", allMediaFiles.size());

        List<AnalyzedMediaFile> analyzedMediaFiles = mediaAnalyzer.analyze(allMediaFiles, input.forceField());

        List<AnalyzedMediaFile> mediaFilesWithDate = analyzedMediaFiles.stream()
                .filter(mediaFile -> mediaFile.getCreationDate() != null)
                .toList();
        List<AnalyzedMediaFile> mediaFilesWithoutDate = analyzedMediaFiles.stream()
                .filter(mediaFile -> mediaFile.getCreationDate() == null)
                .toList();

        console.println("Found %d media files with date, %d media files without date.", mediaFilesWithDate.size(), mediaFilesWithoutDate.size());
        mediaFilesWithoutDate.forEach((mediaFile) -> console.println("No date found for %s", mediaFile.getOriginalPath()));

        List<SetDateAction> setDateActionList = mediaFilesWithDate.stream()
                .map(mediaFile -> createSetDateAction(mediaFile, input.duration()))
                .toList();

        console.println("Total %d files will have date set.", setDateActionList.size());
        setDateActionList.forEach((action) -> console.println("%s", action));

        if (console.confirmAction("Do you want to continue?")) {
            exifDateSetter.setDateTime(setDateActionList);

            if (input.rename()) {
                List<MoveAction> moveActions = createMoveActions(setDateActionList);

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
        } else {
            console.println("Aborted.");
        }
    }

    private SetDateAction createSetDateAction(AnalyzedMediaFile mediaFile, Duration duration) {
        OffsetDateTime originalDateTime = mediaFile.getCreationDate().getDateTime();
        OffsetDateTime shiftedDateTime = originalDateTime.plus(duration);
        MediaDateTime newMediaDate = new MediaDateTime(shiftedDateTime.toLocalDateTime(), shiftedDateTime.getOffset());

        return new SetDateAction(mediaFile.getOriginalPath(), mediaFile.getMediaType(), newMediaDate);
    }

    private List<MoveAction> createMoveActions(List<SetDateAction> setDateActions) {
        List<MoveAction> rawMoveActions = new ArrayList<>();

        for (var setDateAction : setDateActions) {
            var originalPath = setDateAction.file();

            var mediaFile = new AnalyzedMediaFile(originalPath, setDateAction.mediaType(), Collections.emptyMap(), setDateAction.dateTime());
            var newName = fileNameUtils.createNewName(mediaFile);
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
