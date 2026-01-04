package sk.kubisoft.exifutils.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EnvironmentUtils {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentUtils.class);
    private static final String APP_NAME = "exifutils";

    private EnvironmentUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * This handles:
     * <p>
     * Windows: C:\Users\<username>\AppData\Roaming\exifutils\
     * macOS: ~/Library/Application Support/exifutils/
     * Linux: ~/.config/exifutils/ (or custom $XDG_CONFIG_HOME if set)
     */
    public static Path getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        Path configRootDirectory = switch (os) {
            case String s when s.contains("win") -> Path.of(System.getenv("APPDATA"));
            case String s when s.contains("mac") -> Path.of(userHome, "Library", "Application Support");
            default -> // Linux and others follow XDG Base Directory Specification
                    Path.of(
                            System.getenv("XDG_CONFIG_HOME") != null
                                    ? System.getenv("XDG_CONFIG_HOME")
                                    : Path.of(userHome, ".config").toString()
                    );
        };

        var appConfigDirectory = configRootDirectory.resolve(APP_NAME);
        if (!Files.exists(appConfigDirectory)) {
            try {
                Files.createDirectories(appConfigDirectory);
                logger.info("Created config directory: {}", appConfigDirectory);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to create directory: " + appConfigDirectory, e);
            }
        }

        return appConfigDirectory;
    }
}
