package sk.kubisoft.exifutils.setdate;

import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.config.model.ExifToolConfig;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileNameAnalyzer;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.metadata.MediaTypeDetector;
import sk.kubisoft.exifutils.core.metadata.MetaDataSetter;

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

    @Inject
    public SetDateCommand(Console console, ConfigService configService, FileNameAnalyzer fileNameAnalyzer,
                          FileExplorer fileExplorer, MediaTypeDetector mediaTypeDetector) {
        this.console = console;
        this.configService = configService;
        this.fileNameAnalyzer = fileNameAnalyzer;
        this.fileExplorer = fileExplorer;
        this.mediaTypeDetector = mediaTypeDetector;
    }

    public void execute(SetDateCommandInput input) {
        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured");
        }

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

        console.println("Total %d files will have date set:", setDateActionList.size());
        setDateActionList.forEach((action) -> console.println("%s", action));

        if (console.confirmAction("Do you want to continue?")) {
            console.println("Setting datetime to files...");
           performSetDateTime(setDateActionList, exifToolConfig);
        } else {
            console.println("Aborted.");
        }
    }

    private void performSetDateTime(List<SetDateAction> setDateActionList, ExifToolConfig exifToolConfig) {
        try (var metaDataSetter = new MetaDataSetter(exifToolConfig.getPath())) {
            for (var action : setDateActionList) {
                console.println("Setting date and time for file: %s", action.file());

                metaDataSetter.setDateTime(action.file(), action.mediaType(), action.dateTime());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }
    }

    private List<SetDateAction> setDateTimeManually(List<Path> allFiles, OffsetDateTime offsetDateTime) {
        console.println("Setting date and time for files to: %s", offsetDateTime);

        List<SetDateAction> actions = new ArrayList<>();
        for (var file : allFiles) {
            var mediaDate = new MediaDateTime(offsetDateTime);
            var mediaType = mediaTypeDetector.getMediaType(file);
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
                var offsetToUse = ZoneOffset.systemDefault().getRules().getOffset(localDateTime);
                var mediaDate = new MediaDateTime(localDateTime, offsetToUse);
                var mediaType = mediaTypeDetector.getMediaType(file);
                actions.add(new SetDateAction(file, mediaType, mediaDate));
            });
        }

        return actions;
    }
}
