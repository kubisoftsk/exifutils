package sk.kubisoft.exifutils.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.List;

@Singleton
public class InfoCommand {

    private final FileExplorer fileExplorer;
    private final MediaAnalyzer mediaAnalyzer;
    private final Console console;
    private final ObjectMapper objectMapper;

    @Inject
    public InfoCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                       Console console, ObjectMapper objectMapper) {
        this.fileExplorer = fileExplorer;
        this.mediaAnalyzer = mediaAnalyzer;
        this.console = console;
        this.objectMapper = objectMapper;
    }

    public void execute(InfoCommandInput input) {
        console.verboseln("Running ExifUtils Info command with input: %s", input);

        console.println("Searching for media files...");
        List<Path> allFiles = fileExplorer.listFiles(input.paths());
        console.println("Found %d files.", allFiles.size());

        List<MediaFile> mediaFiles = mediaAnalyzer.analyze(allFiles);

        for (MediaFile mediaFile : mediaFiles) {
            MediaType mediaType = mediaFile.mediaType();
            console.println("File %s type is: %s.", mediaFile.originalPath(), mediaType);

            var metaData = mediaFile.metadata();
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaData);
                console.println(json);

                if (mediaFile.creationDate() != null) {
                    console.println("Extracted created date: %s", mediaFile.creationDate());
                } else {
                    console.println("No date found.");
                }
            } catch (Exception e) {
                console.error("Error serializing metadata to JSON: %s", e.getMessage());
            }
            console.println("");
        }
    }

}
