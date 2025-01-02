package sk.kubisoft.exifsort;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileExplorer {

    private static final List<String> COMMON_VIDEO_EXTENSIONS = List.of("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "vob", "m4v", "3gp", "3g2", "mpg", "mpeg", "m2v", "m4v", "ts", "mts", "m2ts", "asf", "rm", "rmvb", "ogv", "ogg", "drc", "dat", "m2p", "m2ts", "k3g", "skm", "evo", "nsv", "pva", "tp", "tpr", "ts", "trp", "m2t", "m2ts", "mts");
    private static final List<String> COMMON_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "heic", "heif");

    private final Tika tika = new Tika();

    public List<Path> listFiles(List<Path> inputDirs) {
        List<Path> allPaths = new ArrayList<>();
        for (var inputDir : inputDirs) {

            try (var filesStream = Files.walk(inputDir)) {
                var inputDirFiles = filesStream
                        .filter(path -> path.toFile().isFile() && isImageOrVideoFile(path))
                        .toList();
                allPaths.addAll(inputDirFiles);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return allPaths;
    }

    private boolean isImageOrVideoFile(Path path) {
        String extension = FilenameUtils.getExtension(path.toString());
        if (COMMON_VIDEO_EXTENSIONS.contains(extension)) {
            return true;
        }
        if (COMMON_IMAGE_EXTENSIONS.contains(extension)) {
            return false;
        }

        try {
            String mimeType = tika.detect(path);

            // Check if the MIME type indicates a video file or an image file
            return mimeType.startsWith("video") || mimeType.startsWith("image");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
