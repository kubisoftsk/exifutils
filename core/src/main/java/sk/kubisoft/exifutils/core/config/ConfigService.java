package sk.kubisoft.exifutils.core.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.model.ExifUtilsConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public final class ConfigService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

	private static final String APP_NAME = "exifsort";
	private static final String CONFIG_FILE_NAME = "exifsort-config.yml";

	private ExifUtilsConfiguration config;

	@Inject
	public ConfigService() {
		loadConfig();
	}

	public ExifUtilsConfiguration getConfig() {
		return config;
	}

	/**
	 * This handles:
	 *
	 * Windows: C:\Users\<username>\AppData\Roaming\exifsort\
	 * macOS: ~/Library/Application Support/exifsort/
	 * Linux: ~/.config/exifsort/ (or custom $XDG_CONFIG_HOME if set)
	 */
	private Path getConfigurationDirectory() {
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

	private void loadConfig() {
		Path configDir = getConfigurationDirectory();
		Path configFile = configDir.resolve(CONFIG_FILE_NAME);

		if (!Files.exists(configFile)) {
			System.out.println("Config file does not exist: " + configFile);
			createDefaultConfig(configDir, configFile);
		}

		org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
		try (InputStream is = Files.newInputStream(configFile)) {
			config = yaml.loadAs(is, ExifUtilsConfiguration.class);
			logger.info("Loaded config file: {}", configFile);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load config file: " + configFile, e);
		}
	}

	private void createDefaultConfig(Path configDir, Path configFile) {
		// create the directory if it does not exist
		try {
			Files.createDirectories(configDir);
			logger.info("Created directory: {}", configDir);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to create directory: " + configDir, e);
		}

		// copy default config from resources to file
		try (var defaultConfigIs = getClass().getResourceAsStream("/configuration-default.yml");
			 Writer writer = Files.newBufferedWriter(configFile)) {
			if (defaultConfigIs == null) {
				throw new IllegalStateException("Default config file not found in resources");
			}
			IOUtils.copy(defaultConfigIs, writer, StandardCharsets.UTF_8);
			logger.info("Created default config file: {}", configFile.toAbsolutePath());
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to create config file: " + configFile, e);
		}
	}

}
