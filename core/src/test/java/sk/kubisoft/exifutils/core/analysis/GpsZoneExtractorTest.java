package sk.kubisoft.exifutils.core.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GpsZoneExtractorTest {

    private GpsZoneExtractor gpsZoneExtractor;

    @BeforeEach
    void setUp() {
        gpsZoneExtractor = new GpsZoneExtractor();
    }

    @Test
    void testExtractGpsEmptyForNullLatAndLong() {
        var zoneOptional = gpsZoneExtractor.extractGpsZone(createMetadata(null, null));

        assertThat(zoneOptional).isNotPresent();
    }

    @Test
    void testExtractGpsEmptyForEmptyLatAndLong() {
        var zoneOptional = gpsZoneExtractor.extractGpsZone(createMetadata("", ""));

        assertThat(zoneOptional).isNotPresent();
    }

    @Test
    void testExtractGpsEmptyForZeroLatAndLong() {
        var zoneOptional = gpsZoneExtractor.extractGpsZone(createMetadata("0", "0"));

        assertThat(zoneOptional).isNotPresent();
    }

    @Test
    void testExtractGpsZoneBratislava() {
        var latitude = "48.123456";
        var longitude = "17.123456";

        var zoneOptional = gpsZoneExtractor.extractGpsZone(createMetadata(latitude, longitude));

        assertThat(zoneOptional).isPresent();
        assertThat(zoneOptional).hasValue(ZoneId.of("Europe/Bratislava"));
    }

    private Map<String, String> createMetadata(String latitude, String longitude) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("GPSLatitude", latitude);
        metadata.put("GPSLongitude", longitude);
        return metadata;
    }

}