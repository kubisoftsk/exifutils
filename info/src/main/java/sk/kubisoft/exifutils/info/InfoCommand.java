package sk.kubisoft.exifutils.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class InfoCommand {

    private final FileExplorer fileExplorer;
    private final Console console;
    private final ObjectMapper objectMapper;

    @Inject
    public InfoCommand(FileExplorer fileExplorer, Console console, ObjectMapper objectMapper) {
        this.fileExplorer = fileExplorer;
        this.console = console;
        this.objectMapper = objectMapper;
    }

    public void execute(InfoCommandInput input) {
        console.verboseln("Running ExifUtils Info command with input: %s", input);

        console.println("Searching for media files...");
        List<MediaFile> mediaFiles = fileExplorer.listMediaFiles(input.inputPaths());
        console.println("Found %d files.", mediaFiles.size());

        for (MediaFile mediaFile : mediaFiles) {
            MediaType mediaType = mediaFile.mediaType();
            console.println("File %s:", mediaFile.originalPath());
            console.println("Resolved created date: %s", (mediaFile.creationDate()) != null ? mediaFile.creationDate() : "N/A");
            if (input.printAll()) {
                console.println("Media type: %s", mediaType);
                console.println("EXIF metadata:");
                var metaData = mediaFile.metadata();
                try {
                    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaData);
                    console.println(json);
                } catch (Exception e) {
                    console.error("Error serializing metadata to JSON: %s", e.getMessage());
                }
            }
            console.println("");
        }
    }

}
