package sk.kubisoft.exifutils.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.analysis.MediaDateExtractor;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class InfoCommand {

    private final FileExplorer fileExplorer;
    private final MediaAnalyzer mediaAnalyzer;
    private final MediaDateExtractor mediaDateExtractor;
    private final Console console;
    private final ObjectMapper objectMapper;

    @Inject
    public InfoCommand(FileExplorer fileExplorer, MediaAnalyzer mediaAnalyzer,
                       MediaDateExtractor mediaDateExtractor, Console console, ObjectMapper objectMapper) {
        this.fileExplorer = fileExplorer;
        this.mediaDateExtractor = mediaDateExtractor;
        this.mediaAnalyzer = mediaAnalyzer;
        this.console = console;
        this.objectMapper = objectMapper;
    }

    public void execute(InfoCommandInput input) {
        console.verboseln("Running ExifUtils Info command with input: %s", input);

        List<Path> allFiles = input.paths();
        if (allFiles.stream().anyMatch(Files::isDirectory)) {
            console.println("Searching for media files...");
            // todo extract to FileExplorer
            allFiles = traverseDirectories(input.paths());
            console.println("Found %d files.", allFiles.size());
        }

        List<MediaFile> mediaFiles = mediaAnalyzer.getMetaData(allFiles);

        for (MediaFile mediaFile : mediaFiles) {
            MediaType mediaType = mediaFile.mediaType();
            console.println("File %s type is: %s.", mediaFile.originalPath(), mediaType);

            var metaData = mediaFile.metadata();
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaData);
                console.println(json);
                if (input.extractDate()) {
                    var extractedDateOptional = mediaDateExtractor.extractCreationDate(mediaFile);
                    if (extractedDateOptional.isPresent()) {
                        var extractedDate = extractedDateOptional.get();
                        console.println("Extracted date: %s", extractedDate);
                    } else {
                        console.println("No date extracted.");
                    }
                }
            } catch (Exception e) {
                console.error("Error serializing metadata to JSON: %s", e.getMessage());
            }
            console.println("");
        }
    }

    private List<Path> traverseDirectories(List<Path> inputPath) {
        List<Path> allPaths = new ArrayList<>();
        for (var path : inputPath) {
            if (path.toFile().isDirectory()) {
                allPaths.addAll(fileExplorer.listFiles(List.of(path)));
            } else {
                allPaths.add(path);
            }
        }
        return allPaths;
    }
}
