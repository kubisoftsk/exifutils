package sk.kubisoft.exifutils.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;

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

    @Inject
    public SortCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                       MediaFileSorter mediaFileSorter, FileMover fileMover, Console console) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.mediaFileSorter = mediaFileSorter;
        this.fileMover = fileMover;
        this.console = console;
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
        mediaFilesWithoutDate.forEach((mediaFile) -> console.verboseln("No date found for %s", mediaFile.originalPath()));

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
