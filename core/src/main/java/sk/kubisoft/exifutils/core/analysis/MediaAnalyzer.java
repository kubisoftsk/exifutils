package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MediaAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(MediaAnalyzer.class);

    private final Console console;
    private final ConfigService configService;
    private final MediaTypeDetector mediaTypeDetector;
    private final ExifDateExtractor exifDateExtractor;
    private final GpsZoneExtractor gpsZoneExtractor;

    @Inject
    public MediaAnalyzer(Console console, ConfigService configService, MediaTypeDetector mediaTypeDetector,
                         ExifDateExtractor exifDateExtractor, GpsZoneExtractor gpsZoneExtractor) {
        this.console = console;
        this.configService = configService;
        this.mediaTypeDetector = mediaTypeDetector;
        this.exifDateExtractor = exifDateExtractor;
        this.gpsZoneExtractor = gpsZoneExtractor;
    }

    public List<MediaFile> analyze(List<Path> files) {
        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured");
        }
        List<MediaFile> mediaFiles = new ArrayList<>();
        console.println("Starting analysis of media files...", files.size());
        try (var metaDataExtractor = new MetaDataExtractor(exifToolConfig.getPath())) {
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);

                if (console.isVerbose()) {
                    console.println("Analyzing file %d of %d: %s", i + 1, files.size(), file);
                } else {
                    console.progress("Analyzing file %d of %d: %s", i + 1, files.size(), file);
                }

                try {
                    MediaFile mediaFile = analyze(file, metaDataExtractor, gpsZoneExtractor);
                    mediaFiles.add(mediaFile);
                } catch (Exception e) {
                    console.error("Error processing file: %s", e, file);
                }
            }
            if (!console.isVerbose()) {
                console.progress(""); // Clear progress line
            }
            console.println("Analysis finished.");
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }
        return mediaFiles;
    }

    private MediaFile analyze(Path file, MetaDataExtractor metaDataExtractor, GpsZoneExtractor gpsZoneExtractor) {
        MediaType mediaType = mediaTypeDetector.detectMediaType(file);
        Map<String, String> metadata = metaDataExtractor.extractMetaData(file);
        Optional<ExifDateTime> extractedDateOptional = exifDateExtractor.extractCreationDate(metadata);
        if (extractedDateOptional.isEmpty()) {
            console.verboseln("No date found in metadata");
            return new MediaFile(file, mediaType, metadata, null);
        }
        ExifDateTime extractedDate = extractedDateOptional.get();
        if (extractedDate.zoneOffset() == null) {
            console.verboseln("No date with offset found, guessing offset...");
            var localDateTime = extractedDate.localDateTime();
            ZoneOffset offsetToUse = guessZoneOffset(file, localDateTime, metadata, gpsZoneExtractor);
            extractedDate = new ExifDateTime(localDateTime, offsetToUse);
        }

        /*
        if (mediaType == MediaType.VIDEO) {
            // assume the video date is in UTC time, this is important for videos, because historically
            // quick time videos has the date in UTC time, so we must convert it to local time with guessed offset
            OffsetDateTime utcDateTime = localDateTime.atOffset(ZoneOffset.UTC);
            var localTimeAtOffsetSameInstant = utcDateTime.withOffsetSameInstant(offsetToUse).toLocalDateTime();
            return Optional.of(new MediaDateTime(localTimeAtOffsetSameInstant, offsetToUse));
        } else if (mediaType == MediaType.IMAGE) {
            // similar to video, but we assume the image date is in local time, so we don't need to convert it, just use it
            return Optional.of(new MediaDateTime(localDateTime, offsetToUse));
        } else {
            throw new IllegalArgumentException("Unknown media type: " + mediaType);
        }

*/
        MediaDateTime mediaDateTime = new MediaDateTime(extractedDate.localDateTime(), extractedDate.zoneOffset());
        return new MediaFile(file, mediaType, metadata, mediaDateTime);
    }

    private ZoneOffset guessZoneOffset(Path file, LocalDateTime localDateTime, Map<String, String> metadata, GpsZoneExtractor gpsZoneExtractor) {
        var gpsZoneOffsetOptional = getGpsZoneOffset(file, gpsZoneExtractor, localDateTime, metadata);
        var defaultTimeZone = getDefaultZoneOffset(localDateTime);

        return gpsZoneOffsetOptional.orElse(defaultTimeZone);
    }

    private Optional<ZoneOffset> getGpsZoneOffset(Path file, GpsZoneExtractor gpsZoneExtractor, LocalDateTime localDateTime, Map<String, String> metadata) {
        var zoneIdOptional = gpsZoneExtractor.extractGpsZone(file, metadata);

        return zoneIdOptional.map(zoneId -> {
            console.verboseln("Found zoneId from GPS coordinates: %s", zoneId);
            return zoneId.getRules().getOffset(localDateTime);
        });
    }

    private ZoneOffset getDefaultZoneOffset(LocalDateTime localDateTime) {
        var config = configService.getConfig();
        var dateTimeConfig = config.getDateTime();
        if (dateTimeConfig != null && dateTimeConfig.getTimeZone() != null) {
            console.verboseln("Using configured time zone: %s", dateTimeConfig.getTimeZone());
            ZoneId zoneId = ZoneId.of(dateTimeConfig.getTimeZone());
            return zoneId.getRules().getOffset(localDateTime);
        }
        // fallback to system default
        console.verboseln("Using system default time zone: %s", ZoneOffset.systemDefault());
        return ZoneOffset.systemDefault().getRules().getOffset(localDateTime);
    }

}
