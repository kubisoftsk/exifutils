package sk.kubisoft.exifutils.core.utils;

import java.nio.file.Path;

public final class EnvironmentUtils {

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

        return configRootDirectory.resolve(APP_NAME);
    }

    /**
     * Returns platform-specific logs directory:
     * <p>
     * Windows: C:\Users\<username>\AppData\Local\exifutils\logs\
     * macOS: ~/Library/Logs/exifutils/
     * Linux: ~/.local/share/exifutils/logs/ (or custom $XDG_DATA_HOME if set)
     */
    public static Path getLogsDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        return switch (os) {
            case String s when s.contains("win") -> Path.of(System.getenv("LOCALAPPDATA"), APP_NAME, "logs");
            case String s when s.contains("mac") -> Path.of(userHome, "Library", "Logs", APP_NAME);
            default -> // Linux and others follow XDG Base Directory Specification
                    Path.of(
                            System.getenv("XDG_DATA_HOME") != null
                                    ? System.getenv("XDG_DATA_HOME")
                                    : Path.of(userHome, ".local", "share").toString(),
                            APP_NAME, "logs"
                    );
        };
    }
}
