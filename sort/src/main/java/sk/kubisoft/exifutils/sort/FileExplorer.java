package sk.kubisoft.exifutils.sort;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class FileExplorer {

    @Inject
    public FileExplorer() {
    }

    public List<Path> listFiles(List<Path> inputDirs) {
        Set<Path> allPaths = new HashSet<>();
        for (var inputDir : inputDirs) {

            try (var filesStream = Files.walk(inputDir)) {
                var inputDirFiles = filesStream
                        .filter(path -> path.toFile().isFile())
                        .toList();
                allPaths.addAll(inputDirFiles);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return allPaths.stream()
                .sorted()
                .toList();
    }

}
