package sk.kubisoft.exifutils.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Singleton
public class FileMover {

    private static final Logger logger = LoggerFactory.getLogger(FileMover.class);
    private final DuplicateFileHandler duplicateHandler;

    @Inject
    public FileMover(DuplicateFileHandler duplicateHandler) {
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
                    logger.error("Source file does not exist: {}", source);
                    continue;
                }

                if (!Files.isRegularFile(source)) {
                    logger.error("Source is not a regular file: {}", source);
                    continue;
                }

                // Handle potential file conflicts
                Path resolvedTarget = duplicateHandler.resolveConflict(source, target);
                if (!resolvedTarget.equals(target)) {
                    logger.info("Resolved file conflict: {} -> {}", target, resolvedTarget);
                    target = resolvedTarget;
                }

                // Create target directories
                Path targetDir = target.getParent();
                try {
                    Files.createDirectories(targetDir);
                } catch (IOException e) {
                    logger.error("Failed to create target directory {}: {}", targetDir, e.getMessage());
                    continue;
                }

                // Rest of the existing move logic...
                if (!Files.isWritable(source)) {
                    logger.error("Source file is not writable: {}", source);
                    continue;
                }

                if (targetDir != null && !Files.isWritable(targetDir)) {
                    logger.error("Target directory is not writable: {}", targetDir);
                    continue;
                }

                // Perform atomic move
                logger.info("Moving {} to {}", source, target);
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
                logger.error("Security violation moving {} to {}: {}", source, target, e.getMessage());
            } catch (IOException e) {
                logger.error("IO error moving {} to {}: {}", source, target, e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error moving {} to {}: {}", source, target, e.getMessage());
            }
        }

        logger.info("Move operation completed. Successfully moved {} out of {} files",
                successCount, moveActions.size());
    }
}
