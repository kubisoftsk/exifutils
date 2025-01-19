package sk.kubisoft.exifutils.core.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EnvironmentUtils {

    private static final String APP_NAME = "exifsort";

    private EnvironmentUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * This handles:
     * <p>
     * Windows: C:\Users\<username>\AppData\Roaming\exifsort\
     * macOS: ~/Library/Application Support/exifsort/
     * Linux: ~/.config/exifsort/ (or custom $XDG_CONFIG_HOME if set)
     */
    public static Path getApplicationDirectory() {
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

        var applicationDirectory = configRootDirectory.resolve(APP_NAME);
        // create the directory if it does not exist
        try {
            Files.createDirectories(applicationDirectory);
            // TODO try
            //logger.info("Created directory: {}", configDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create directory: " + applicationDirectory, e);
        }

        return applicationDirectory;
    }
}
