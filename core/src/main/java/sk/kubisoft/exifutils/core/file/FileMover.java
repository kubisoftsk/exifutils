package sk.kubisoft.exifutils.core.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;

@Singleton
public class FileMover {

    private static final Logger logger = LoggerFactory.getLogger(FileMover.class);
    private final DuplicateFileHandler duplicateHandler;
    private final Console console;

    @Inject
    public FileMover(Console console, DuplicateFileHandler duplicateHandler) {
        this.console = console;
        this.duplicateHandler = duplicateHandler;
    }

    public void moveFiles(Map<Path, Path> moveActions) {
        int successCount = 0;

        for (Map.Entry<Path, Path> action : moveActions.entrySet()) {
            Path source = action.getKey();
            Path target = action.getValue();

            try {
                // Basic validation
                if (!Files.exists(source)) {
                    console.error("Source file does not exist: %s", source);
                    continue;
                }

                if (!Files.isRegularFile(source)) {
                    console.error("Source is not a regular file: %s", source);
                    continue;
                }

                // Handle potential file conflicts
                Path resolvedTarget = duplicateHandler.resolveConflict(source, target);
                if (!resolvedTarget.equals(target)) {
                    console.verbose("Resolved file conflict: %s -> %s", target, resolvedTarget);
                    target = resolvedTarget;
                }

                // Create target directories
                Path targetDir = target.getParent();
                try {
                    Files.createDirectories(targetDir);
                } catch (IOException e) {
                    console.error("Failed to create target directory %s: %s", targetDir, e.getMessage());
                    continue;
                }

                // Rest of the existing move logic...
                if (!Files.isWritable(source)) {
                    console.error("Source file is not writable: %s", source);
                    continue;
                }

                if (targetDir != null && !Files.isWritable(targetDir)) {
                    console.error("Target directory is not writable: %s", targetDir);
                    continue;
                }

                // Perform atomic move
                if (Objects.equals(target.getParent(), source.getParent())) {
                    console.println("Moving %s to %s", source, target.getFileName());
                } else {
                    console.println("Moving %s to %s", source, target);
                }
                try {
                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                    successCount++;
                    logger.debug("Successfully moved {} to {}", source, target);
                } catch (AtomicMoveNotSupportedException e) {
                    // Fallback to non-atomic move if atomic is not supported
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                    successCount++;
                    logger.debug("Successfully moved (non-atomic) {} to {}", source, target);
                }

            } catch (SecurityException e) {
                console.error("Security violation moving %s to %s: %s", source, target, e.getMessage());
            } catch (IOException e) {
                console.error("IO error moving %s to %s: %s", source, target, e.getMessage());
            } catch (Exception e) {
                console.error("Unexpected error moving %s to %s: %s", source, target, e.getMessage());
            }
        }

        console.println("Operation completed. Successfully moved %s out of %s files",
                successCount, moveActions.size());
    }
}
