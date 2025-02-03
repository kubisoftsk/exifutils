package sk.kubisoft.exifutils.core.utils;

public class ExifUtilsException extends RuntimeException {

    public ExifUtilsException(String message) {
        super(message);
    }

    public ExifUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

}
