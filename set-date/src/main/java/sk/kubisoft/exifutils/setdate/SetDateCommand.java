package sk.kubisoft.exifutils.setdate;

import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileNameAnalyzer;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetDateCommand {

    private final Console console;
    private final ConfigService configService;
    private final FileExplorer fileExplorer;
    private final FileNameAnalyzer fileNameAnalyzer;
    private final MediaTypeDetector mediaTypeDetector;
    private final ExifDateSetter exifDateSetter;

    @Inject
    public SetDateCommand(Console console, ConfigService configService, FileNameAnalyzer fileNameAnalyzer,
                          FileExplorer fileExplorer, MediaTypeDetector mediaTypeDetector, ExifDateSetter exifDateSetter) {
        this.console = console;
        this.configService = configService;
        this.fileNameAnalyzer = fileNameAnalyzer;
        this.fileExplorer = fileExplorer;
        this.mediaTypeDetector = mediaTypeDetector;
        this.exifDateSetter = exifDateSetter;
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
