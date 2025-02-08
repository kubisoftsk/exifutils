package sk.kubisoft.exifutils.setdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.*;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class SetDateCommand {

    private static final Logger logger = LoggerFactory.getLogger(SetDateCommand.class);

    private final Console console;
    private final ConfigService configService;
    private final FileExplorer fileExplorer;
    private final FileNameAnalyzer fileNameAnalyzer;
    private final MediaTypeDetector mediaTypeDetector;
    private final ExifDateSetter exifDateSetter;
    private final MediaFileNameUtils fileNameUtils;
    private final DuplicatePreProcessor duplicatePreProcessor;
    private final FileMover fileMover;

    @Inject
    public SetDateCommand(Console console, ConfigService configService, FileNameAnalyzer fileNameAnalyzer,
                          FileExplorer fileExplorer, MediaTypeDetector mediaTypeDetector, ExifDateSetter exifDateSetter,
                          MediaFileNameUtils fileNameUtils, DuplicatePreProcessor duplicatePreProcessor, FileMover fileMover) {
        this.console = console;
        this.configService = configService;
        this.fileNameAnalyzer = fileNameAnalyzer;
        this.fileExplorer = fileExplorer;
        this.mediaTypeDetector = mediaTypeDetector;
        this.exifDateSetter = exifDateSetter;
        this.fileNameUtils = fileNameUtils;
        this.duplicatePreProcessor = duplicatePreProcessor;
        this.fileMover = fileMover;
    }

    public void execute(SetDateCommandInput input) {
        console.verboseln("Running ExifUtils Rename command with input: %s", input);

        console.println("Searching for media files...");
        List<Path> allFiles = fileExplorer.listFiles(input.sourcePaths());
        console.println("Found %d files.", allFiles.size());

        List<SetDateAction> setDateActionList;
        if (input.dateTime() != null) {
            setDateActionList = setDateTimeManually(allFiles, input.dateTime());
        } else {
            setDateActionList = listAndParseFromFileNames(allFiles, input.pattern());
        }

        console.println("Total %d files will have date set.", setDateActionList.size());
        setDateActionList.forEach((action) -> console.println("%s", action));

        if (console.confirmAction("Do you want to continue?")) {
            exifDateSetter.setDateTime(setDateActionList);
        } else {
            console.println("Aborted.");
        }

        if (input.rename()) {
            // TODO this is mostly duplicate! refactor
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
    }

    private List<MoveAction> createMoveActions(List<SetDateAction> setDateActions) {
        List<MoveAction> rawMoveActions = new ArrayList<>();

        for (var setDateAction : setDateActions) {
            var originalPath = setDateAction.file();

            // TODO Refactor not to use media file or decide if it is ok
            var mediaFile = new MediaFile(originalPath, setDateAction.mediaType(), Collections.emptyMap(), setDateAction.dateTime());
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

    private List<SetDateAction> setDateTimeManually(List<Path> allFiles, OffsetDateTime offsetDateTime) {
        console.println("Setting date and time for files to: %s", offsetDateTime);

        List<SetDateAction> actions = new ArrayList<>();
        for (var file : allFiles) {
            var mediaDate = new MediaDateTime(offsetDateTime.toLocalDateTime(), offsetDateTime.getOffset());
            var mediaType = mediaTypeDetector.detectMediaType(file);
            actions.add(new SetDateAction(file, mediaType, mediaDate));
        }
        return actions;
    }

    private List<SetDateAction> listAndParseFromFileNames(List<Path> allFiles, String userPattern) {
        // TODO implement also user pattern parsing
        console.println("Setting date and time for files using pattern guessed from file names");

        List<SetDateAction> actions = new ArrayList<>();
        for (var file : allFiles) {
            var dateTimeOptional = fileNameAnalyzer.analyzeFileName(file.getFileName().toString());
            dateTimeOptional.ifPresent(localDateTime -> {
                // TODO take from config service
                var offsetToUse = ZoneOffset.systemDefault().getRules().getOffset(localDateTime);
                var mediaDate = new MediaDateTime(localDateTime, offsetToUse);
                var mediaType = mediaTypeDetector.detectMediaType(file);
                actions.add(new SetDateAction(file, mediaType, mediaDate));
            });
        }

        return actions;
    }
}
