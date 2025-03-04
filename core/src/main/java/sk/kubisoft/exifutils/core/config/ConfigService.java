package sk.kubisoft.exifutils.core.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import sk.kubisoft.exifutils.core.config.model.ExifUtilsConfiguration;
import sk.kubisoft.exifutils.core.utils.EnvironmentUtils;

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

	private static final String CONFIG_FILE_NAME = "exifsort-config.yml";

	private final Yaml yaml;

	private ExifUtilsConfiguration config;

	@Inject
	public ConfigService(Yaml yaml) {
		this.yaml = yaml;
		loadConfig();
	}

	public ExifUtilsConfiguration getConfig() {
		return config;
	}

	private void loadConfig() {
		Path configDir = EnvironmentUtils.getApplicationDirectory();
		Path configFile = configDir.resolve(CONFIG_FILE_NAME);

		if (!Files.exists(configFile)) {
			System.out.println("Config file does not exist: " + configFile);
			createDefaultConfig(configFile);
		}

		try (InputStream is = Files.newInputStream(configFile)) {
			config = yaml.loadAs(is, ExifUtilsConfiguration.class);
			logger.info("Loaded config file: {}", configFile);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load config file: " + configFile, e);
		}
	}

	private void createDefaultConfig(Path configFile) {
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
