package sk.kubisoft.exifutils.core.analysis.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeviceProfileServiceTest {

    private final Yaml yaml = new Yaml();

    private DeviceProfileService deviceProfileService;

    @BeforeEach
    void setUp() {
        deviceProfileService = new DeviceProfileService(yaml);
    }

    @Test
    void getDefaultProfile() {
        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(Collections.emptyMap());

        assertEquals("Default", deviceProfile.getName());
    }

    @Test
    void getNikonD90Profile() {
        Map<String, String> metadata = Map.of("Make", "NIKON", "Model", "NIKON D90");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Nikon D90", deviceProfile.getName());
    }

    @Test
    void getNikonD3100Profile() {
        Map<String, String> metadata = Map.of("Model", "NIKON D3100");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Nikon D3100", deviceProfile.getName());
    }

    @Test
    void getNikonE4800Profile() {
        Map<String, String> metadata = Map.of("Make", "NIKON", "Model", "E4800");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Nikon E4800", deviceProfile.getName());
    }
}