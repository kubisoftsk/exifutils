package sk.kubisoft.exifutils.core.file;

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
