package sk.kubisoft.exifutils.core.analysis.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.junit.jupiter.api.Assertions.*;

class DeviceProfileServiceTest {

	private Yaml yaml = new Yaml();

	private DeviceProfileService deviceProfileService;

	@BeforeEach
	void setUp() {
		deviceProfileService = new DeviceProfileService(yaml);
	}

	@Test
	void getDefaultProfile() {
		DeviceProfile deviceProfile = deviceProfileService.getDefaultProfile();

		assertEquals("default", deviceProfile.getModel());
	}
}