package sk.kubisoft.exifutils.core.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.utils.EnvironmentUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class ConfigService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

	private static final String CONFIG_FILE_NAME = "application.conf";

	private final Config config;

	@Inject
	public ConfigService() {
		this.config = loadConfig();
	}

	private Config loadConfig() {
		Path configDir = EnvironmentUtils.getConfigDirectory();
		Path configFile = configDir.resolve(CONFIG_FILE_NAME);

		if (!Files.exists(configFile)) {
			logger.info("Config file does not exist: {}", configFile);
			createDefaultConfig(configFile);
		}

		// Load user config from file, then fall back to reference.conf from classpath
		Config userConfig = ConfigFactory.parseFile(configFile.toFile());
		Config referenceConfig = ConfigFactory.defaultReference();
		Config merged = userConfig.withFallback(referenceConfig).resolve();

		logger.info("Loaded config from: {}", configFile);
		return merged;
	}

	private void createDefaultConfig(Path configFile) {
		try (var templateStream = getClass().getResourceAsStream("/application-template.conf");
			 Writer writer = Files.newBufferedWriter(configFile)) {
			if (templateStream == null) {
				throw new IllegalStateException("Config template not found in resources");
			}
			IOUtils.copy(templateStream, writer, StandardCharsets.UTF_8);
			logger.info("Created config file from template: {}", configFile.toAbsolutePath());
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to create config file: " + configFile, e);
		}
	}

	public String getExifToolPath() {
		return config.getString("exifTool.path");
	}

	public String getTimeZone() {
		return config.getString("dateTime.timeZone");
	}

	public String getRenamePattern() {
		return config.getString("rename.pattern");
	}

	/**
	 * Returns the underlying Config object for advanced use cases.
	 */
	public Config getConfig() {
		return config;
	}
}
