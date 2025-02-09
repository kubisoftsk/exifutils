package sk.kubisoft.exifutils.core.file;

import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class FileExplorer {

    private final MediaTypeDetector mediaTypeDetector;

    @Inject
    public FileExplorer(MediaTypeDetector mediaTypeDetector) {
        this.mediaTypeDetector = mediaTypeDetector;
    }

    public List<Path> listFiles(List<Path> inputPaths) {
        Set<Path> allPaths = new HashSet<>();
        for (var path : inputPaths) {
            if (path.toFile().isDirectory()) {
                allPaths.addAll(walk(path));
            } else {
                allPaths.add(path);
            }
        }

        return allPaths.stream()
                .sorted()
                .toList();
    }

    public List<MediaFile> listMediaFiles(List<Path> inputPaths) {
        List<MediaFile> mediaFiles = new ArrayList<>();
        for (var path : listFiles(inputPaths)) {
            var mediaType = mediaTypeDetector.detectMediaType(path);
            if (mediaType != null) {
                mediaFiles.add(new MediaFile(path, mediaType));
            }
        }
        return mediaFiles;
    }

    private Set<Path> walk(Path inputDir) {
        Set<Path> allPaths = new HashSet<>();
        try (var filesStream = Files.walk(inputDir)) {
            var inputDirFiles = filesStream
                    .filter(path -> path.toFile().isFile())
                    .toList();
            allPaths.addAll(inputDirFiles);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return allPaths;
    }

}
