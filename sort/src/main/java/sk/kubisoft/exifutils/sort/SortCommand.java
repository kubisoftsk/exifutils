package sk.kubisoft.exifutils.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

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

        Map<MediaFile, MediaDateTime> mediaFilesWithDate = mediaAnalyzer.analyzeCreationDate(allFiles);
        console.println("Found %d media files with valid dates.", mediaFilesWithDate.size());

        List<MoveAction> moveActions = mediaFileSorter.sort(mediaFilesWithDate, input.destinationDirectory(), input.rename());
        console.println("Total %d files will be moved:", moveActions.size());
        moveActions.forEach((moveAction) -> console.println("Move %s", moveAction));

        if (input.dryRun()) {
            logger.info("Dry run, not moving files");
        } else {
            logger.info("Moving files...");
            fileMover.moveFiles(moveActions);
        }
    }

}
