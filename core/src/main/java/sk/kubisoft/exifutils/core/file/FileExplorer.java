package sk.kubisoft.exifutils.core.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.media.MediaFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.function.Function;

@Singleton
public class FileExplorer {

    private static final Logger logger = LoggerFactory.getLogger(FileExplorer.class);

    private final FileService fileService;
    private final MediaTypeDetector mediaTypeDetector;
    private final ConfigService configService;

    @Inject
    public FileExplorer(FileService fileService, MediaTypeDetector mediaTypeDetector, ConfigService configService) {
        this.fileService = fileService;
        this.mediaTypeDetector = mediaTypeDetector;
        this.configService = configService;
    }

    public List<Path> listFiles(String[] args) {
        return listFiles(args, null);
    }

    public List<Path> listFiles(String[] args, FileSortOrder sortOrder) {
        List<Path> inputPaths = Arrays.stream(args)
                .map(Paths::get)
                .toList();

        return listFiles(inputPaths, sortOrder);
    }

    public List<Path> listFiles(Path inputPath) {
        return listFiles(List.of(inputPath), null);
    }

    public List<Path> listFiles(List<Path> inputPaths) {
        return listFiles(inputPaths, null);
    }

    public List<Path> listFiles(List<Path> inputPaths, FileSortOrder sortOrder) {
        Set<Path> allPaths = new HashSet<>();
        for (var path : inputPaths) {
            checkPath(path);
            allPaths.addAll(listPath(path));
        }

        FileSortOrder effectiveOrder = (sortOrder != null) ? sortOrder : configService.getFileSortOrder();

        return allPaths.stream()
                .sorted(getComparator(effectiveOrder))
                .toList();
    }

    public List<MediaFile> listMediaFiles(String[] args) {
        return listMediaFiles(args, null);
    }

    public List<MediaFile> listMediaFiles(String[] args, FileSortOrder sortOrder) {
        List<MediaFile> mediaFiles = new ArrayList<>();
        for (var path : listFiles(args, sortOrder)) {
            var mediaType = mediaTypeDetector.detectMediaType(path);
            if (mediaType != null) {
                mediaFiles.add(new MediaFile(path, mediaType));
            }
        }
        return mediaFiles;
    }

    private Comparator<Path> getComparator(FileSortOrder sortOrder) {
        return switch (sortOrder) {
            case NAME -> Comparator.naturalOrder();
            case LAST_MODIFIED -> compareByAttribute(BasicFileAttributes::lastModifiedTime);
            case CREATED -> compareByAttribute(BasicFileAttributes::creationTime);
        };
    }

    private Comparator<Path> compareByAttribute(Function<BasicFileAttributes, FileTime> timeExtractor) {
        return (p1, p2) -> {
            try {
                var attr1 = Files.readAttributes(p1, BasicFileAttributes.class);
                var attr2 = Files.readAttributes(p2, BasicFileAttributes.class);

                return timeExtractor.apply(attr1).compareTo(timeExtractor.apply(attr2));
            } catch (IOException e) {
                logger.warn("Error reading file attributes for {} or {}: {}", p1, p2, e.getMessage());
                return 0;
            }
        };
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
