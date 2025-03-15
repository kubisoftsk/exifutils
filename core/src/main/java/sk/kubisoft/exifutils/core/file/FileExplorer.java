package sk.kubisoft.exifutils.core.file;

import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Singleton
public class FileExplorer {

    private final FileService fileService;

    private final MediaTypeDetector mediaTypeDetector;

    @Inject
    public FileExplorer(FileService fileService, MediaTypeDetector mediaTypeDetector) {
        this.fileService = fileService;
        this.mediaTypeDetector = mediaTypeDetector;
    }

    public List<Path> listFiles(String[] args) {
        List<Path> inputPaths = Arrays.stream(args)
                .map(Paths::get)
                .toList();

        return listFiles(inputPaths);
    }

    public List<Path> listFiles(Path inputPath) {
        return listFiles(List.of(inputPath));
    }

    public List<Path> listFiles(List<Path> inputPaths) {
        Set<Path> allPaths = new HashSet<>();
        for (var path : inputPaths) {
            checkPath(path);
            allPaths.addAll(listPath(path));
        }

        return allPaths.stream()
                .sorted()
                .toList();
    }

    public List<MediaFile> listMediaFiles(String[] args) {
        List<MediaFile> mediaFiles = new ArrayList<>();
        for (var path : listFiles(args)) {
            var mediaType = mediaTypeDetector.detectMediaType(path);
            if (mediaType != null) {
                mediaFiles.add(new MediaFile(path, mediaType));
            }
        }
        return mediaFiles;
    }

    private List<Path> listPath(Path path) {
        if (fileService.isDirectory(path)) {
            return walk(path);
        } else {
            return List.of(path);
        }
    }

    private void checkPath(Path path) {
        if (!fileService.exists(path)) {
            throw new IllegalArgumentException("Source file / directory does not exist" + path);
        }
        if (!fileService.isReadable(path)) {
            throw new IllegalArgumentException("Cannot read source file / directory: " + path);
        }
    }

    private List<Path> walk(Path inputDir) {
        List<Path> allPaths = new ArrayList<>();
        try (var filesStream = fileService.walk(inputDir)) {
            var inputDirFiles = filesStream
                    .filter(fileService::isRegularFile)
                    .toList();
            allPaths.addAll(inputDirFiles);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return allPaths;
    }

}
