package sk.kubisoft.exifutils.core.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileService {

    boolean isRegularFile(Path path);

    boolean isDirectory(Path path);

    boolean exists(Path path);

    boolean isReadable(Path path);

    Stream<Path> walk(Path inputDir) throws IOException;

}
