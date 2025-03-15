package sk.kubisoft.exifutils.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
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
    private final MediaAnalyzer mediaAnalyzer;

    @Inject
    public InfoCommand(FileExplorer fileExplorer, Console console, ObjectMapper objectMapper, MediaAnalyzer mediaAnalyzer) {
        this.fileExplorer = fileExplorer;
        this.console = console;
        this.objectMapper = objectMapper;
        this.mediaAnalyzer = mediaAnalyzer;
    }

    public void execute(InfoCommandInput input) {
        console.verboseln("Running ExifUtils Info command with input: %s", input);

        console.println("Searching for media files...");
        List<MediaFile> mediaFiles = fileExplorer.listMediaFiles(input.inputPaths());
        console.println("Found %d files.", mediaFiles.size());

        List<AnalyzedMediaFile> analyzedFiles = mediaAnalyzer.analyze(mediaFiles);

        for (AnalyzedMediaFile mediaFile : analyzedFiles) {
            MediaType mediaType = mediaFile.getMediaType();
            console.println("File %s:", mediaFile.getOriginalPath());
            console.println("Resolved created date: %s", (mediaFile.getCreationDate()) != null ? mediaFile.getCreationDate() : "N/A");
            if (input.printAll()) {
                console.println("Media type: %s", mediaType);
                console.println("EXIF metadata:");
                var metaData = mediaFile.getMetadata();
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
