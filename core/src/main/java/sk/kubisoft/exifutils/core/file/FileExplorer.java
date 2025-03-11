package sk.kubisoft.exifutils.core.file;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class FileExplorer {

    private final FileService fileService;

    private final MediaTypeDetector mediaTypeDetector;

    @Inject
    public FileExplorer(FileService fileService, MediaTypeDetector mediaTypeDetector) {
        this.fileService = fileService;
        this.mediaTypeDetector = mediaTypeDetector;
    }

    // supports globbing - https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
    public List<Path> listFiles(String[] args) {
        Set<Path> allPaths = new HashSet<>();

        for (String sourceArg : args) {
            Path path;
            PathMatcher matcher = null;
            if (containsGlobCharacter(sourceArg)) {
                int pathSeparatorIndex = getLastGlobPathSeparatorIndex(sourceArg);
                String globParentPath = sourceArg.substring(0, pathSeparatorIndex);
                path = Paths.get(globParentPath);

                String globPattern = sourceArg.substring(pathSeparatorIndex + 1);
                matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
            } else {
                path = Paths.get(sourceArg);
            }

            checkPath(path);
            if (fileService.isDirectory(path)) {
                allPaths.addAll(walk(path));
            } else {
                allPaths.add(path);
            }
        }

        return allPaths.stream()
                .sorted()
                .toList();
    }

    private void checkPath(Path path) {
        if (!fileService.exists(path)) {
            throw new IllegalArgumentException("Source file / directory does not exist" + path);
        }
        if (!fileService.isReadable(path)) {
            throw new IllegalArgumentException("Cannot read source file / directory: " + path);
        }
    }

    private int getLastGlobPathSeparatorIndex(String sourceArg) {
        int asteriskIndex = sourceArg.indexOf("*");
        int questionMarkIndex = sourceArg.indexOf("?");
        int openBracketIndex = sourceArg.indexOf("[");
        int closeBracketIndex = sourceArg.indexOf("]");

        int firstGlobCharacterIndex = Integer.MAX_VALUE;
        firstGlobCharacterIndex = (asteriskIndex == -1) ? firstGlobCharacterIndex : asteriskIndex;
        firstGlobCharacterIndex = (questionMarkIndex == -1) ? firstGlobCharacterIndex : Math.min(firstGlobCharacterIndex, questionMarkIndex);
        firstGlobCharacterIndex = (openBracketIndex == -1) ? firstGlobCharacterIndex : Math.min(firstGlobCharacterIndex, openBracketIndex);
        firstGlobCharacterIndex = (closeBracketIndex == -1) ? firstGlobCharacterIndex : Math.min(firstGlobCharacterIndex, closeBracketIndex);

        if (firstGlobCharacterIndex == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("No glob character found in path: " + sourceArg);
        }

        var pathSubstring = sourceArg.substring(0, firstGlobCharacterIndex);

        return pathSubstring.lastIndexOf(File.separator);
    }

    private boolean containsGlobCharacter(String sourceArg) {
        return StringUtils.containsAny(sourceArg, "*", "?", "[", "]");
    }

    public List<Path> listFiles(List<Path> inputPaths) {
        Set<Path> allPaths = new HashSet<>();
        for (var path : inputPaths) {
            if (fileService.isDirectory(path)) {
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
        try (var filesStream = fileService.walk(inputDir)) {
            var inputDirFiles = filesStream
                    .filter(path -> fileService.isRegularFile(path))
                    .toList();
            allPaths.addAll(inputDirFiles);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return allPaths;
    }

}
