package sk.kubisoft.exifutils.core.analysis.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Singleton
public class DeviceProfileService {

	private static final String RESOURCE_PATH = "/device-profiles/";
	private static final String DEFAULT_PROFILE_FILE = "default.yml";

	private static final Logger logger = LoggerFactory.getLogger(DeviceProfileService.class);

	private static final List<String> RESOURCE_PROFILE_FILE_NAMES;

	static {
		List<String> files = new ArrayList<>();
		files.add("nikon-d90.yml");
		files.add("nikon-d3100.yml");
		files.add("nikon-e4800.yml");
		files.add("olympus-fe120.yml");
		files.add("kodak-m1033.yml");

		RESOURCE_PROFILE_FILE_NAMES = Collections.unmodifiableList(files);
	}

	private final Yaml yaml;

	private final DeviceProfile defaultProfile;

	private final List<DeviceProfile> profiles;

	@Inject
	public DeviceProfileService(Yaml yaml) {
		this.yaml = yaml;
		defaultProfile = loadProfile(DEFAULT_PROFILE_FILE);
		this.profiles = Collections.unmodifiableList(loadResourceProfiles());
	}

	private List<DeviceProfile> loadResourceProfiles() {
		List<DeviceProfile> profiles = new ArrayList<>();
		for (var fileName : RESOURCE_PROFILE_FILE_NAMES) {
			profiles.add(loadProfile(fileName));
		}
		return profiles;
	}

	private DeviceProfile loadProfile(String profileResourceFileName) {
		try (var inputStream = getClass().getResourceAsStream(RESOURCE_PATH + profileResourceFileName)) {
			var profile = yaml.loadAs(inputStream, DeviceProfile.class);
			logger.info("Loaded device profile: {}", profile.getName());
			return profile;
		} catch (Exception e) {
			throw new RuntimeException("Failed to load device profile: " + profileResourceFileName, e);
		}
	}

	public DeviceProfile getProfileForTags(Map<String, String> metadata) {
		for (var profile : profiles) {
			if (profileMatches(profile, metadata)) {
				return profile;
			}
		}
		return defaultProfile;
	}

	private boolean profileMatches(DeviceProfile profile, Map<String, String> metadata) {
		Map<String, String> profileTagsMap = profile.getTags();
		if (profileTagsMap.size() > metadata.size()) {
			return false;
		}
		for (var entry : profileTagsMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (!value.equals(metadata.get(key))) {
				return false;
			}
		}
		return true;
	}
}
