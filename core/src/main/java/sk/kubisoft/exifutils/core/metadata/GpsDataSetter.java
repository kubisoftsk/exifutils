package sk.kubisoft.exifutils.core.metadata;

import sk.kubisoft.exifutils.core.file.SetLocationAction;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
public class GpsDataSetter {

    // Constants for the GPS EXIF Tags
    private static final String GPS_LATITUDE_REF = "GPSLatitudeRef";
    private static final String GPS_LATITUDE = "GPSLatitude";
    private static final String GPS_LONGITUDE_REF = "GPSLongitudeRef";
    private static final String GPS_LONGITUDE = "GPSLongitude";

    private final Console console;

    private final MetaDataHandlerFactory metaDataHandlerFactory;

    @Inject
    public GpsDataSetter(Console console, MetaDataHandlerFactory metaDataHandlerFactory) {
        this.console = console;
        this.metaDataHandlerFactory = metaDataHandlerFactory;
    }

    public void setLocationData(List<SetLocationAction> actionList) {
        try (var metaDataSetter = metaDataHandlerFactory.create()) {
            for (int i = 0; i < actionList.size(); i++) {
                var action = actionList.get(i);

                if (console.isVerbose()) {
                    console.println("Setting GPS data for file %d of %d: %s", i + 1, actionList.size(), action.mediaFile().getOriginalPath());
                } else {
                    console.progress("Setting GPS data for file %d of %d: %s", i + 1, actionList.size(), action.mediaFile().getOriginalPath());
                }

                setLocation(metaDataSetter, action.mediaFile().getOriginalPath(), action.latitude(), action.longitude());

                if (console.isVerbose()) {
                    console.println(""); // Append newline after each file in verbose mode for clarity
                }
            }
            if (!console.isVerbose()) {
                console.progress(""); // Clear progress line
            }
            console.println("Setting GPS data to files finished.");
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }
    }

    private void setLocation(MetaDataHandler metaDataSetter, Path path, Double latitude, Double longitude) {
        Map<String, String> newTags;
        if (latitude == null && longitude == null) {
            newTags = createNullTags();
        } else {
            newTags = createGpsTags(latitude, longitude);
        }

        metaDataSetter.setMetaDataTags(path, newTags);
    }

    private Map<String, String> createGpsTags(Double latitude, Double longitude) {
        Map<String, String> newTags = new HashMap<>();
        newTags.put(GPS_LATITUDE_REF, latitude >= 0 ? "N" : "S");
        newTags.put(GPS_LATITUDE, convertToExifGpsFormat(latitude));
        newTags.put(GPS_LONGITUDE_REF, longitude >= 0 ? "E" : "W");
        newTags.put(GPS_LONGITUDE, convertToExifGpsFormat(longitude));
        return newTags;
    }

    private String convertToExifGpsFormat(double decimal) {
        // Get the absolute value of the decimal
        double abs = Math.abs(decimal);

        // Degrees is the integer part
        int degrees = (int) abs;

        // Multiply remainder by 60 to get minutes
        double minutesDecimal = (abs - degrees) * 60;
        int minutes = (int) minutesDecimal;

        // Multiply remainder by 60 again to get seconds
        double seconds = (minutesDecimal - minutes) * 60;

        return String.format(Locale.US, "%d %d %.8f", degrees, minutes, seconds);
    }

    private Map<String, String> createNullTags() {
        Map<String, String> newTags = new HashMap<>();
        newTags.put("GPS*", "");
        return newTags;
    }
}
