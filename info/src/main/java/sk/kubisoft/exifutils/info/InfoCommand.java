package sk.kubisoft.exifutils.info;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class InfoCommand {

    private final FileExplorer fileExplorer;
    private final Console console;
    private final MediaAnalyzer mediaAnalyzer;

    @Inject
    public InfoCommand(FileExplorer fileExplorer, Console console, MediaAnalyzer mediaAnalyzer) {
        this.fileExplorer = fileExplorer;
        this.console = console;
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
                printFormattedMetadata(metaData);
            }
            console.println("");
        }
    }

    private void printFormattedMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            console.println("  (no metadata)");
            return;
        }

        // Sort metadata alphabetically
        Map<String, String> sortedMetadata = new TreeMap<>(metadata);

        // Find the longest key for padding
        int maxKeyLength = sortedMetadata.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        // Print each key-value pair with aligned values
        sortedMetadata.forEach((key, value) -> {
            String paddedKey = StringUtils.rightPad(key, maxKeyLength);
            console.println("  %s : %s", paddedKey, value);
        });
    }

}
