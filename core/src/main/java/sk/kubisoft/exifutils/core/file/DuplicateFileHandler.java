package sk.kubisoft.exifutils.core.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

@Singleton
public class DuplicateFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateFileHandler.class);
    private static final int BUFFER_SIZE = 8192;
    private Console console;

    @Inject
    public DuplicateFileHandler(Console console) {
        this.console = console;
    }

    /**
     * Generates a Path that doesn't conflict with existing files.
     * If a file exists, appends (1), (2), etc. before the extension.
     * Also checks if files are identical using checksums.
     *
     * @param sourcePath The source path of the file
     * @param targetPath The desired target path
     * @return A non-conflicting Path, or the original if the files are identical
     */
    public Path resolveConflict(Path sourcePath, Path targetPath) throws IOException {
        if (!Files.exists(targetPath)) {
            return targetPath;
        }

        // First, check if the files are identical
        if (areFilesIdentical(sourcePath, targetPath)) {
            logger.debug("Files are identical: {} and {}", sourcePath, targetPath);
            return targetPath;
        } else {
            logger.debug("Files are different: {} and {}", sourcePath, targetPath);
        }

        // If not identical, find a new name
        String fileName = targetPath.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        int counter = 1;
        Path newPath;
        do {
            String newFileName = String.format("%s (%d)%s", baseName, counter++, extension);
            newPath = targetPath.getParent().resolve(newFileName);
        } while (Files.exists(newPath));

        return newPath;
    }

    /**
     * Compares two files using a fast CRC32 check first, then SHA-256 if CRC32 matches.
     *
     * @param file1 First file to compare
     * @param file2 Second file to compare
     * @return true if files are identical, false otherwise
     */
    private boolean areFilesIdentical(Path file1, Path file2) throws IOException {
        // Quick check - if file sizes differ, files are different
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }

        // First do a fast CRC32 check
        if (!haveSameCRC32(file1, file2)) {
            return false;
        }

        // If CRC32 matches, do a full SHA-256 comparison
        return haveSameSHA256(file1, file2);
    }

    private boolean haveSameCRC32(Path file1, Path file2) throws IOException {
        CRC32 crc1 = calculateCRC32(file1);
        CRC32 crc2 = calculateCRC32(file2);
        return crc1.getValue() == crc2.getValue();
    }

    private CRC32 calculateCRC32(Path file) throws IOException {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[BUFFER_SIZE];

        try (InputStream is = Files.newInputStream(file)) {
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }
        return crc;
    }

    private boolean haveSameSHA256(Path file1, Path file2) throws IOException {
        byte[] hash1 = calculateSHA256(file1);
        byte[] hash2 = calculateSHA256(file2);

        if (hash1.length != hash2.length) {
            return false;
        }

        for (int i = 0; i < hash1.length; i++) {
            if (hash1[i] != hash2[i]) {
                return false;
            }
        }
        return true;
    }

    private byte[] calculateSHA256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BUFFER_SIZE];

            try (InputStream is = Files.newInputStream(file)) {
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            // This should never happen as SHA-256 is required to be supported by all Java implementations
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}