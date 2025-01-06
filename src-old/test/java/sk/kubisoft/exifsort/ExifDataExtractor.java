package sk.kubisoft.exifsort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ExifDataExtractor {

    /**
     * Utility to extract EXIF metadata from media files and output as JSON.
     *
     * Required environment variables:
     * - MEDIA_FILE_PATH: Path to the media file to analyze
     * - EXIF_TOOL_PATH: Path to ExifTool executable
     */
    public static void main(String[] args) throws Exception {
        String mediaFilePath = Objects.requireNonNull(System.getenv("MEDIA_FILE_PATH"), "MEDIA_FILE_PATH environment variable not set");
        String exifToolPath = Objects.requireNonNull(System.getenv("EXIF_TOOL_PATH"), "EXIF_TOOL_PATH environment variable not set");

        try(ExifTool exifTool = new ExifToolBuilder().withPath(new File(exifToolPath)).build()) {
            Map<Tag, String> metadata = exifTool.getImageMeta(new File(mediaFilePath));
            Map<String, String> metaDataStrings = new TreeMap<>();

            metadata.forEach((key1, value) -> {
                String key = key1.getName();
                metaDataStrings.put(key, value);
            });

            String[] sensitiveKeysToRemove = {
                    "Directory",
                    "FilePermissions",
                    "GPSAltitude",
                    "GPSAltitudeRef",
                    "GPSCoordinates",
                    "GPSLatitude",
                    "GPSLongitude",
                    "GPSPosition",
                    "LocationAccuracyHorizontal",
                    "ZoneIdentifier",
                    "FileName"
            };
            Arrays.stream(sensitiveKeysToRemove).forEach(metaDataStrings::remove);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaDataStrings);
            System.out.println(json);
        }
    }
}
