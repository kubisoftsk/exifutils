package sk.kubisoft.exifutils.core.analysis.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void getOlympusFE120Profile() {
        Map<String, String> metadata = Map.of("Make", "OLYMPUS DIGITAL CAMERA", "Model", "FE-120,X-700");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Olympus FE120", deviceProfile.getName());
        assertEquals("CreationDate", deviceProfile.getVideoFields().get(0).getDateField());
        assertEquals("TrackCreateDate", deviceProfile.getVideoFields().get(1).getDateField());
    }

    @Test
    void getOlympusX3Profile() {
        Map<String, String> metadata = Map.of("Make", "OLYMPUS DIGITAL CAMERA", "Model", "D4194");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Olympus X-3", deviceProfile.getName());
        assertEquals("CreationDate", deviceProfile.getVideoFields().get(0).getDateField());
        assertEquals("TrackCreateDate", deviceProfile.getVideoFields().get(1).getDateField());
        assertTrue(deviceProfile.getVideoFields().get(1).isLocalTime());
    }

    @Test
    void getOlympusU15DProfile() {
        Map<String, String> metadata = Map.of("Make", "OLYMPUS DIGITAL CAMERA", "Model", "D4216");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Olympus u15D", deviceProfile.getName());
        assertEquals("CreationDate", deviceProfile.getVideoFields().get(0).getDateField());
        assertEquals("TrackCreateDate", deviceProfile.getVideoFields().get(1).getDateField());
        assertTrue(deviceProfile.getVideoFields().get(1).isLocalTime());
    }

    @Test
    void getFujifilmFinePixA330Profile() {
        Map<String, String> metadata = Map.of("Make", "FUJIFILM", "Model", "FinePix A330");

        DeviceProfile deviceProfile = deviceProfileService.getProfileForTags(metadata);

        assertEquals("Fujifilm FinePix A330", deviceProfile.getName());
        assertEquals("DateTimeOriginal", deviceProfile.getVideoFields().get(0).getDateField());
        assertTrue(deviceProfile.getVideoFields().get(0).isLocalTime());
    }
}