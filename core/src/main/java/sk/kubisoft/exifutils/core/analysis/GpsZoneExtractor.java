package sk.kubisoft.exifutils.core.analysis;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class GpsZoneExtractor {

    private static final Logger logger = LoggerFactory.getLogger(GpsZoneExtractor.class);

    private static final String GPS_LATITUDE_TAG = "GPSLatitude";
    private static final String GPS_LONGITUDE_TAG = "GPSLongitude";

    private final AtomicReference<TimeZoneEngine> engineAtomicReference = new AtomicReference<>();

    @Inject
    public GpsZoneExtractor() {
    }

    public Optional<ZoneId> extractGpsZone(Path path, Map<String, String> metadata) {
        String gpsLatitude = metadata.get(GPS_LATITUDE_TAG);
        String gpsLongitude = metadata.get(GPS_LONGITUDE_TAG);
        if (StringUtils.isBlank(gpsLatitude) || StringUtils.isBlank(gpsLongitude)) {
            return Optional.empty();
        }
        var latitude = Double.parseDouble(gpsLatitude);
        var longitude = Double.parseDouble(gpsLongitude);
        if (latitude == 0 && longitude == 0) {
            return Optional.empty();
        }
        try {
            return extractGpsZone(latitude, longitude);
        } catch (Exception e) {
            throw new AnalysisException(path, "Error extracting GPS zone", e);
        }
    }

    public Optional<ZoneId> extractGpsZone(double latitude, double longitude) {
        TimeZoneEngine engine = engineAtomicReference.updateAndGet(engineRef ->
                engineRef != null ? engineRef : TimeZoneEngine.initialize());

        return engine.query(latitude, longitude);
    }

}
