package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;
import sk.kubisoft.exifutils.core.metadata.MetaDataHandler;
import sk.kubisoft.exifutils.core.metadata.MetaDataHandlerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
	private final MetaDataHandlerFactory metaDataHandlerFactory;
	private final ExifDateExtractor exifDateExtractor;
	private final GpsZoneExtractor gpsZoneExtractor;

	@Inject
	public MediaAnalyzer(Console console, ConfigService configService, MediaTypeDetector mediaTypeDetector,
						 MetaDataHandlerFactory metaDataHandlerFactory, ExifDateExtractor exifDateExtractor, GpsZoneExtractor gpsZoneExtractor) {
		this.console = console;
		this.configService = configService;
		this.mediaTypeDetector = mediaTypeDetector;
		this.metaDataHandlerFactory = metaDataHandlerFactory;
		this.exifDateExtractor = exifDateExtractor;
		this.gpsZoneExtractor = gpsZoneExtractor;
	}

	public List<MediaFile> analyze(List<Path> files) {
		List<MediaFile> mediaFiles = new ArrayList<>();
		console.println("Starting analysis of media files...", files.size());
		try (var metaDataHandler = metaDataHandlerFactory.create()) {
			for (int i = 0; i < files.size(); i++) {
				var file = files.get(i);

				if (console.isVerbose()) {
					console.println("Analyzing file %d of %d: %s", i + 1, files.size(), file);
				} else {
					console.progress("Analyzing file %d of %d: %s", i + 1, files.size(), file);
				}

				try {
					MediaFile mediaFile = analyze(file, metaDataHandler, gpsZoneExtractor);
					mediaFiles.add(mediaFile);
				} catch (Exception e) {
					console.error("Error processing file: %s", e, file);
				}
				if (console.isVerbose()) {
					console.println(""); // Append newline after each file in verbose mode for clarity
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

	private MediaFile analyze(Path file, MetaDataHandler metaDataHandler, GpsZoneExtractor gpsZoneExtractor) {
		MediaType mediaType = mediaTypeDetector.detectMediaType(file);
		Map<String, String> metadata = metaDataHandler.extractMetaData(file);
		Optional<ExifDateTime> extractedDateOptional = exifDateExtractor.extractCreationDate(metadata);
		if (extractedDateOptional.isEmpty()) {
			console.verboseln("No date found in metadata");
			return new MediaFile(file, mediaType, metadata, null);
		}
		ExifDateTime extractedDate = extractedDateOptional.get();
		if (extractedDate.zoneOffset() == null) {
			console.verboseln("Zone offset is missing, resolving offset...");
			var localDateTime = extractedDate.localDateTime();
			ZoneOffset offsetToUse = resolveZoneOffset(file, localDateTime, metadata, gpsZoneExtractor);

			if (mediaType == MediaType.IMAGE) {
				// assume the image local date is in local time, so we don't need to convert it, just assign it
				extractedDate = new ExifDateTime(localDateTime, offsetToUse);
			} else if (mediaType == MediaType.VIDEO) {
				// assume the video local date without offset is in UTC time, this is important for videos, because historically
				// quick time videos has the date in UTC time, so we must convert it to local time with guessed offset
				console.verboseln("Converting video date from UTC to local time with offset: %s", offsetToUse);
				OffsetDateTime utcDateTime = localDateTime.atOffset(ZoneOffset.UTC);
				var localTimeAtOffsetSameInstant = utcDateTime.withOffsetSameInstant(offsetToUse).toLocalDateTime();
				extractedDate = new ExifDateTime(localTimeAtOffsetSameInstant, offsetToUse);
			} else {
				throw new IllegalArgumentException("Unknown media type: " + mediaType);
			}
		}

		MediaDateTime mediaDateTime = new MediaDateTime(extractedDate.localDateTime(), extractedDate.zoneOffset());
		console.verboseln("Resolved create date: %s", mediaDateTime);
		return new MediaFile(file, mediaType, metadata, mediaDateTime);
	}

	private ZoneOffset resolveZoneOffset(Path file, LocalDateTime localDateTime, Map<String, String> metadata, GpsZoneExtractor gpsZoneExtractor) {
		var gpsZoneOffset = getGpsZoneOffset(file, gpsZoneExtractor, localDateTime, metadata);
		if (gpsZoneOffset != null) {
			return gpsZoneOffset;
		} else {
			console.verboseln("No GPS metadata found.");
			return getDefaultZoneOffset(localDateTime);
		}
	}

	private ZoneOffset getGpsZoneOffset(Path file, GpsZoneExtractor gpsZoneExtractor, LocalDateTime localDateTime, Map<String, String> metadata) {
		var zoneIdOptional = gpsZoneExtractor.extractGpsZone(file, metadata);

		return zoneIdOptional.map(zoneId -> {
			console.verboseln("Found zoneId from GPS coordinates: %s", zoneId);
			return zoneId.getRules().getOffset(localDateTime);
		}).orElse(null);
	}

	private ZoneOffset getDefaultZoneOffset(LocalDateTime localDateTime) {
		var config = configService.getConfig();
		var dateTimeConfig = config.getDateTime();
		if (dateTimeConfig != null && dateTimeConfig.getTimeZone() != null) {
			console.verboseln("Resolving offset by configured time zone: %s", dateTimeConfig.getTimeZone());
			ZoneId zoneId = ZoneId.of(dateTimeConfig.getTimeZone());
			return zoneId.getRules().getOffset(localDateTime);
		}
		// fallback to system default
		console.verboseln("Resolving offset by system default time zone: %s", ZoneId.systemDefault());
		return ZoneId.systemDefault().getRules().getOffset(localDateTime);
	}

}
