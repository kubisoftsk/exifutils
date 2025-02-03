package sk.kubisoft.exifutils.core.analysis;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

class GpsZoneExtractor {

    private static final Logger logger = LoggerFactory.getLogger(GpsZoneExtractor.class);

    private static final String GPS_LATITUDE_TAG = "GPSLatitude";
    private static final String GPS_LONGITUDE_TAG = "GPSLongitude";

    private final TimeZoneEngine engine;

    public GpsZoneExtractor() {
        this.engine = TimeZoneEngine.initialize();
    }

    // TODO path only needed for exception handling, refactor to remove
    Optional<ZoneId> extractGpsZone(Path path, Map<String, String> metadata) {
        String gpsLatitude = metadata.get(GPS_LATITUDE_TAG);
        String gpsLongitude = metadata.get(GPS_LONGITUDE_TAG);
        if (StringUtils.isBlank(gpsLatitude) || StringUtils.isBlank(gpsLongitude)) {
            return Optional.empty();
        }
        try {
            return extractGpsZone(gpsLatitude, gpsLongitude);
        } catch (Exception e) {
            throw new AnalysisException(path, "Error extracting GPS zone", e);
        }
    }

    Optional<ZoneId> extractGpsZone(String gpsLatitude, String gpsLongitude) {
        var latitude = Double.parseDouble(gpsLatitude);
        var longitude = Double.parseDouble(gpsLongitude);
        return engine.query(latitude, longitude);
    }

}
