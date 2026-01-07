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
import java.util.List;

@Singleton
public class FileMover {

    private static final Logger logger = LoggerFactory.getLogger(FileMover.class);
    private final Console console;

    @Inject
    public FileMover(Console console) {
        this.console = console;
    }

    public void moveFiles(List<MoveAction> moveActions) {
        processFiles(moveActions, false);
    }

    public void copyFiles(List<MoveAction> moveActions) {
        processFiles(moveActions, true);
    }

    private void processFiles(List<MoveAction> moveActions, boolean copy) {
        int successCount = 0;

        for (var action : moveActions) {
            Path source = action.source();
            Path target = action.target();

            try {
                // Basic validation
                if (!Files.exists(source)) {
                    console.errorln("Source file does not exist: %s", source);
                    continue;
                }

                if (!Files.isRegularFile(source)) {
                    console.errorln("Source is not a regular file: %s", source);
                    continue;
                }

                // Create target directories
                Path targetDir = target.getParent();
                try {
                    Files.createDirectories(targetDir);
                } catch (IOException e) {
                    console.errorln("Failed to create target directory %s: %s", targetDir, e.getMessage());
                    continue;
                }

                // Check source is writable only for move operations
                if (!copy && !Files.isWritable(source)) {
                    console.errorln("Source file is not writable: %s", source);
                    continue;
                }

                if (!Files.isWritable(targetDir)) {
                    console.errorln("Target directory is not writable: %s", targetDir);
                    continue;
                }
                // Fail if target file already exists
                if (Files.exists(target)) {
                    console.errorln("Target file already exists, skipping: %s", target);
                    continue;
                }

                if (copy) {
                    console.println("Copying %s", action);
                    Files.copy(source, target);
                    successCount++;
                    logger.debug("Successfully copied {} to {}", source, target);
                } else {
                    console.println("Moving %s", action);
                    // Perform atomic move
                    try {
                        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                        successCount++;
                        logger.debug("Successfully moved {} to {}", source, target);
                    } catch (AtomicMoveNotSupportedException e) {
                        // Fallback to non-atomic move if atomic is not supported
                        Files.move(source, target);
                        successCount++;
                        logger.debug("Successfully moved (non-atomic) {} to {}", source, target);
                    }
                }
            } catch (SecurityException e) {
                console.errorln("Security violation %s %s to %s: %s", copy ? "copying" : "moving", source, target, e.getMessage());
            } catch (IOException e) {
                console.errorln("IO error %s %s to %s: %s", copy ? "copying" : "moving", source, target, e.getMessage());
            } catch (Exception e) {
                console.errorln("Unexpected error %s %s to %s: %s", copy ? "copying" : "moving", source, target, e.getMessage());
            }
        }

        console.println("Operation completed. Successfully %s %s out of %s files",
                copy ? "copied" : "moved", successCount, moveActions.size());
    }
}
