package sk.kubisoft.exifutils.core.file;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class FilesService {

    // TODO probably delete this class

    @Inject
    public FilesService() {
    }

    public List<Path> listFiles(Path path) {
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        if (!Files.isDirectory(path)) {
            throw new FileException(path, "Path exists but is not a directory");
        }

        try (var listStream = Files.list(path)) {
            return listStream
                    .filter(Files::isRegularFile)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileException(path, "Error listing directory contents", e);
        }
    }
}
