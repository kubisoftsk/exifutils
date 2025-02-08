package sk.kubisoft.exifutils.core.file;

import sk.kubisoft.exifutils.core.utils.ExifUtilsException;

import java.nio.file.Path;

public class FileException extends ExifUtilsException {

    private final Path path;

    public FileException(Path path, String message) {
        super(String.format("Error processing file '%s': %s", path, message));
        this.path = path;
    }

    public FileException(Path path, String message, Throwable cause) {
        super(String.format("Error processing file '%s': %s", path, message), cause);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
