package sk.kubisoft.exifutils.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SortCommand {

    private static final Logger logger = LoggerFactory.getLogger(SortCommand.class);

    private final FileExplorer fileExplorer;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaFileSorter mediaFileSorter;
    private final FileMover fileMover;
    private final Console console;
    private final ExifDateSetter exifDateSetter;

    @Inject
    public SortCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                       MediaFileSorter mediaFileSorter, FileMover fileMover, Console console,
                       ExifDateSetter exifDateSetter) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.mediaFileSorter = mediaFileSorter;
        this.fileMover = fileMover;
        this.console = console;
        this.exifDateSetter = exifDateSetter;
    }

    public void execute(SortCommandInput input) {
        console.verboseln("Running ExifUtils Sort command with input: {}", input);

        console.println("Searching for media files...");
        var allFiles = fileExplorer.listFiles(input.sourceDirectories());
        console.println("Found %d files.", allFiles.size());

        var allMediaFiles = mediaAnalyzer.analyze(allFiles);
        List<MediaFile> mediaFilesWithDate = allMediaFiles.stream()
                .filter(mediaFile -> mediaFile.creationDate() != null)
                .toList();
        List<MediaFile> mediaFilesWithoutDate = allMediaFiles.stream()
                .filter(mediaFile -> mediaFile.creationDate() == null)
                .toList();

        console.println("Found %d media files with date, %d media files without date.", mediaFilesWithDate.size(), mediaFilesWithoutDate.size());
        mediaFilesWithoutDate.forEach((mediaFile) -> console.println("No date found for %s", mediaFile.originalPath()));

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

        List<MoveAction> moveActions = mediaFileSorter.sort(mediaFilesWithDate, input.destinationDirectory(), input.rename());

        if (moveActions.isEmpty()) {
            console.println("No files to move.");
        }

        console.println("Total %d files will be moved:", moveActions.size());
        moveActions.forEach((moveAction) -> console.println("Move %s", moveAction));

        if (console.confirmAction("Do you want to continue?")) {
            console.println("Moving files...");
            fileMover.moveFiles(moveActions);
        } else {
            console.println("Aborted.");
        }
    }

}
