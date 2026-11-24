package sk.kubisoft.exifutils.core.file;

import org.apache.commons.cli.ParseException;

/**
 * Thrown when an --include or --exclude pattern is not valid or cannot match any file name.
 */
public class InvalidFilterPatternException extends ParseException {

    /**
     * Exit code for invalid arguments.
     */
    public static final int EXIT_CODE = 2;

    public InvalidFilterPatternException(String message) {
        super(message);
    }
}
