package sk.kubisoft.exifutils.core.file;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
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
            if (containsGlobCharacter(sourceArg)) {
                allPaths.addAll(processGlobArg(sourceArg));
            } else {
                allPaths.addAll(processPathArg(sourceArg));
            }
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

    public List<Path> listFiles(Path inputPath) {
        return listFiles(List.of(inputPath));
    }

    public List<Path> listFiles(List<Path> inputPaths) {
        Set<Path> allPaths = new HashSet<>();
        for (var path : inputPaths) {
            allPaths.addAll(listPath(path));
        }

        return allPaths.stream()
                .sorted()
                .toList();
    }

    private List<Path> processGlobArg(String sourceArg) {
        int pathSeparatorIndex = getLastGlobPathSeparatorIndex(sourceArg);
        String globRootPath = sourceArg.substring(0, pathSeparatorIndex);
        String globPattern = sourceArg.substring(pathSeparatorIndex + 1);
        Path globRoot = Paths.get(globRootPath);
        checkPath(globRoot);

        List<Path> allDirs = new ArrayList<>();
        allDirs.add(globRoot);
        try (var filesStream = fileService.walk(globRoot)) {
            filesStream.filter(fileService::isDirectory)
                    .forEach(allDirs::add);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<>() {
            @Override
            public boolean accept(Path entry)  {
                return fileService.isRegularFile(entry) && matcher.matches(entry.getFileName());
            }
        };

        List<Path> allPaths = new ArrayList<>();
        for (Path directory : allDirs) {
            try(var directoryStream = fileService.newDirectoryStream(directory, filter)) {
                for (Path path : directoryStream) {
                    allPaths.add(path);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return allPaths;
    }

    private List<Path> processPathArg(String sourceArg) {
        Path path = Paths.get(sourceArg);
        checkPath(path);

        return listPath(path);
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
