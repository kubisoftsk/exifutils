package sk.kubisoft.exifsort.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ConfigService {

    private static final String MEDIADUPE_DIR_NAME = ".mediadupe";
    private static final String CONFIG_FILE_NAME = "mediadupe.properties";

    private static ConfigService instance;

    private ExifSortConfiguration config;

    private ConfigService() {
    }

    public static ConfigService getInstance() {
        // not the best way to do it, but it's good enough for this
        if (instance == null) {
            synchronized (ConfigService.class) {
                if (instance == null) {
                    instance = new ConfigService();
                    instance.loadConfig();
                }
            }
        }
        return instance;
    }

    public ExifSortConfiguration getConfig() {
        return config;
    }

	private void loadConfig() {
		org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();

		/*
		Yaml yaml = new Yaml(new Constructor(ExifSortConfiguration.class));
		try (InputStream in = Files.newInputStream(configPath)) {
			return yaml.load(in);
		}

		 */
	}

	/*
    private void loadConfig() {
        String userHome = System.getProperty("user.home");

        if (userHome == null) {
            throw new IllegalStateException("Expected user.home system property to be set");
        }

        Path mediadupeDir = Paths.get(userHome).resolve(MEDIADUPE_DIR_NAME);
        Path configFile = mediadupeDir.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            System.out.println("Config file does not exist: " + configFile);
            createDefaultConfig(mediadupeDir, configFile);
        }

        Properties properties = new Properties();
        try {
            properties.load(Files.newBufferedReader(configFile));
            System.out.println("Loaded config file: " + configFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load config file: " + configFile, e);
        }

        this.config = parseConfig(properties);
    }

    private void createDefaultConfig(Path mediadupeDir, Path configFile) {
        // create the directory if it does not exist
        try {
            Files.createDirectories(mediadupeDir);
            System.out.println("Created directory: " + mediadupeDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create directory: " + mediadupeDir, e);
        }

        Properties properties = new Properties();
        properties.setProperty(ConfigProperties.INDEX_ENABLED, "false");
        properties.setProperty(ConfigProperties.INDEX_PATH, "");

        try (Writer writer = Files.newBufferedWriter(configFile)) {
            properties.store(writer, "MediaDupe configuration");
            System.out.println("Created config file: " + configFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create config file: " + configFile, e);
        }
    }


    private ExifSortConfiguration parseConfig(Properties properties) {
		ExifSortConfiguration config = new ExifSortConfiguration();

        if (properties.containsKey(ConfigProperties.INDEX_ENABLED)) {
            var indexEnabled = Boolean.parseBoolean(properties.getProperty(ConfigProperties.INDEX_ENABLED));
            config.setIndexEnabled(indexEnabled);
        }

        if (properties.containsKey(ConfigProperties.INDEX_PATH)) {
            var indexPath = Paths.get(properties.getProperty(ConfigProperties.INDEX_PATH));
            config.setIndexPath(indexPath);
        }

        return config;
    }
	*/
}
