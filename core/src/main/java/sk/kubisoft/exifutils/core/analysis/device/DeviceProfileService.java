package sk.kubisoft.exifutils.core.analysis.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DeviceProfileService {

	private static final String RESOURCE_PATH = "/device-profiles/";
	private static final String DEFAULT_PROFILE = "default";

	private static final Logger logger = LoggerFactory.getLogger(DeviceProfileService.class);

	private final Yaml yaml;

	private final Map<String, DeviceProfile> profiles;

	@Inject
	public DeviceProfileService(Yaml yaml) {
		this.yaml = yaml;
		var profileMap = new HashMap<String, DeviceProfile>();

		add(profileMap, DEFAULT_PROFILE);
		add(profileMap, "nikon-d3100");

		this.profiles = Collections.unmodifiableMap(profileMap);
	}

	private void add(HashMap<String, DeviceProfile> profileMap, String profileFileName) {
		try (var inputStream = getClass().getResourceAsStream(RESOURCE_PATH + profileFileName + ".yml")) {
			var profile = yaml.loadAs(inputStream, DeviceProfile.class);
			logger.info("Loaded device profile: {}", profile.getModel());
			profileMap.put(profile.getModel(), profile);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load device profile: " + profileFileName, e);
		}
	}

	public DeviceProfile getDefaultProfile() {
		return profiles.get(DEFAULT_PROFILE);
	}

	public DeviceProfile getProfileForModel(String model) {
		if (profiles.containsKey(model)) {
			return profiles.get(model);
		} else {
			return getDefaultProfile();
		}
	}
}
