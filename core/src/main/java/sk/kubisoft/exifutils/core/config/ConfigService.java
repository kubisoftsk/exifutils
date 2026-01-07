package sk.kubisoft.exifutils.core.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.utils.EnvironmentUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Optional;

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
		Config referenceConfig = ConfigFactory.defaultReference();

		Path configDir = EnvironmentUtils.getConfigDirectory();
		Path configFile = configDir.resolve(CONFIG_FILE_NAME);

		if (Files.exists(configFile)) {
			Config userConfig = ConfigFactory.parseFile(configFile.toFile());
			Config merged = userConfig.withFallback(referenceConfig).resolve();
			logger.info("Loaded config from: {}", configFile);
			return merged;
		}

		logger.debug("No user config found, using defaults from reference.conf");
		return referenceConfig.resolve();
	}

	public String getExifToolPath() {
		return config.getString("exifTool.path");
	}

	/**
	 * Returns the configured time zone, or system default if not configured.
	 */
	public ZoneId getTimeZone() {
		String timeZone = config.getString("dateTime.timeZone");
		if (timeZone == null || timeZone.isEmpty()) {
			return ZoneId.systemDefault();
		}
		return ZoneId.of(timeZone);
	}

	public String getRenamePattern() {
		return config.getString("rename.pattern");
	}

	public String getSortPattern() {
		return config.getString("sort.pattern");
	}

	/**
	 * Returns the configured default destination for sort command, or empty if not configured.
	 */
	public Optional<Path> getSortDestination() {
		String destination = config.getString("sort.destination");
		if (destination == null || destination.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Paths.get(destination));
	}

}
