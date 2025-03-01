package sk.kubisoft.exifutils.setdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.*;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;
import sk.kubisoft.exifutils.core.utils.DateTimeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    private final ExifDateSetter exifDateSetter;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaFileNameUtils fileNameUtils;
    private final DuplicatePreProcessor duplicatePreProcessor;
    private final FileMover fileMover;

    @Inject
    public SetDateCommand(Console console, ConfigService configService, FileNameAnalyzer fileNameAnalyzer,
                          FileExplorer fileExplorer, ExifDateSetter exifDateSetter, MediaAnalyzer mediaAnalyzer,
                          MediaFileNameUtils fileNameUtils, DuplicatePreProcessor duplicatePreProcessor, FileMover fileMover) {
        this.console = console;
        this.configService = configService;
        this.fileNameAnalyzer = fileNameAnalyzer;
        this.fileExplorer = fileExplorer;
        this.exifDateSetter = exifDateSetter;
        this.mediaAnalyzer = mediaAnalyzer;
        this.fileNameUtils = fileNameUtils;
        this.duplicatePreProcessor = duplicatePreProcessor;
        this.fileMover = fileMover;
    }

    public void execute(SetDateCommandInput input) {
        console.verboseln("Running ExifUtils Rename command with input: %s", input);

        console.println("Searching for media files...");
        List<MediaFile> allMediaFiles = fileExplorer.listMediaFiles(input.sourcePaths());
        console.println("Found %d files.", allMediaFiles.size());

        List<MediaFile> mediaFiles;
        if (input.unknownOnly()) {
            var analyzedMediaFiles = mediaAnalyzer.analyze(allMediaFiles.stream().map(MediaFile::originalPath).toList());
            mediaFiles = analyzedMediaFiles.stream()
                    .filter(mediaFile -> mediaFile.creationDate() == null)
                    .toList();
        } else {
            mediaFiles = allMediaFiles;
        }

        List<SetDateAction> setDateActionList;
        if (input.localDateTime() != null) {
            setDateActionList = setDateTimeManually(mediaFiles, input.localDateTime(), input.zoneId());
        } else {
            setDateActionList = listAndParseFromFileNames(mediaFiles, input.zoneId(), input.pattern());
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

    private List<SetDateAction> setDateTimeManually(List<MediaFile> mediaFiles, LocalDateTime localDateTime, ZoneId zoneId) {
        var offsetDateTime = OffsetDateTime.of(localDateTime, getOffset(localDateTime, zoneId));
        console.println("Setting date and time for files to: %s", offsetDateTime);

        List<SetDateAction> actions = new ArrayList<>();
        for (int i = 0; i < mediaFiles.size(); i++) {
            var mediaFile = mediaFiles.get(i);

            // If we have more files, increment the manually supply date by one second, to avoid many conflicts
            // Since we are supplying date, it is not desired to have the same date up to second precision for all files
            // so we increment every date by one second for every file
            var localDateToUse = localDateTime.plusSeconds(i);
            var mediaDate = new MediaDateTime(localDateToUse, offsetDateTime.getOffset());

            actions.add(new SetDateAction(mediaFile.originalPath(), mediaFile.mediaType(), mediaDate));
        }
        return actions;
    }

    private List<SetDateAction> listAndParseFromFileNames(List<MediaFile> mediaFiles, ZoneId zoneId, String userPattern) {
        // TODO implement also user pattern parsing
        console.println("Setting date and time for files using pattern guessed from file names");

        List<SetDateAction> actions = new ArrayList<>();
        for (var mediaFile : mediaFiles) {
            var dateTimeOptional = fileNameAnalyzer.analyzeFileName(mediaFile.originalPath().getFileName().toString());
            dateTimeOptional.ifPresent(localDateTime -> {
                var offsetToUse = getOffset(localDateTime, zoneId);
                var mediaDate = new MediaDateTime(localDateTime, offsetToUse);
                actions.add(new SetDateAction(mediaFile.originalPath(), mediaFile.mediaType(), mediaDate));
            });
        }

        return actions;
    }

    private ZoneOffset getOffset(LocalDateTime localDateTime, ZoneId zoneId) {
        ZoneId zoneIdToUse = zoneId;
        if (zoneIdToUse == null) {
            var config = configService.getConfig();
            var dateTimeConfig = config.getDateTime();
            String timeZone = (dateTimeConfig == null) ? null : dateTimeConfig.getTimeZone();
            if (timeZone != null) {
                zoneIdToUse = ZoneId.of(timeZone);
            }
        }

        return DateTimeUtils.getDefaultZoneOffset(localDateTime, zoneIdToUse);
    }
}
