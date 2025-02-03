package sk.kubisoft.exifutils.core.analysis;

import sk.kubisoft.exifutils.core.utils.ExifUtilsException;

import java.nio.file.Path;

public class AnalysisException extends ExifUtilsException {

    private final Path path;

    public AnalysisException(Path path, String message) {
        super(String.format("Error analyzing file '%s': %s", path, message));
        this.path = path;
    }

    public AnalysisException(Path path, String message, Throwable cause) {
        super(String.format("Error analyzing file '%s': %s", path, message), cause);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
