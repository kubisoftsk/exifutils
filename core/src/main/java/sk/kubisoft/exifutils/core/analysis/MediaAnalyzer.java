package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.metadata.MetaDataHandler;
import sk.kubisoft.exifutils.core.metadata.MetaDataHandlerFactory;
import sk.kubisoft.exifutils.core.utils.DateTimeUtils;

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
	private final MetaDataHandlerFactory metaDataHandlerFactory;
	private final ExifDateExtractor exifDateExtractor;
	private final GpsZoneExtractor gpsZoneExtractor;

	@Inject
	public MediaAnalyzer(Console console, ConfigService configService, MetaDataHandlerFactory metaDataHandlerFactory,
						 ExifDateExtractor exifDateExtractor, GpsZoneExtractor gpsZoneExtractor) {
		this.console = console;
		this.configService = configService;
		this.metaDataHandlerFactory = metaDataHandlerFactory;
		this.exifDateExtractor = exifDateExtractor;
		this.gpsZoneExtractor = gpsZoneExtractor;
	}

	public List<AnalyzedMediaFile> analyze(List<MediaFile> files) {
		List<AnalyzedMediaFile> analyzedFiles = new ArrayList<>();
		console.println("Starting analysis of media files...", files.size());
		try (var metaDataHandler = metaDataHandlerFactory.create()) {
			for (int i = 0; i < files.size(); i++) {
				MediaFile file = files.get(i);
				Path path = file.getOriginalPath();

				if (console.isVerbose()) {
					console.println("Analyzing file %d of %d: %s", i + 1, files.size(), path);
				} else {
					console.progress("Analyzing file %d of %d: %s", i + 1, files.size(), path);
				}

				try {
					AnalyzedMediaFile mediaFile = analyze(file, metaDataHandler, gpsZoneExtractor);
					analyzedFiles.add(mediaFile);
				} catch (Exception e) {
					console.error("Error processing file: %s", e, path);
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
		return analyzedFiles;
	}

	private AnalyzedMediaFile analyze(MediaFile mediaFile, MetaDataHandler metaDataHandler, GpsZoneExtractor gpsZoneExtractor) {
		Map<String, String> metadata = metaDataHandler.extractMetaData(mediaFile.getOriginalPath());
		Optional<ExifDateTime> extractedDateOptional = exifDateExtractor.extractCreationDate(mediaFile.getMediaType(), metadata);
		if (extractedDateOptional.isEmpty()) {
			console.verboseln("No date found in metadata");
			return new AnalyzedMediaFile(mediaFile.getOriginalPath(), mediaFile.getMediaType(), metadata, null);
		}
		ExifDateTime extractedDate = extractedDateOptional.get();
		LocalDateTime localDateTimeToUse = extractedDate.localDateTime();
		ZoneOffset zoneOffsetToUse = extractedDate.zoneOffset();

		if (zoneOffsetToUse == null) {
			console.verboseln("Zone offset is missing, resolving offset...");
			var localDateTime = extractedDate.localDateTime();
			zoneOffsetToUse = resolveZoneOffset(mediaFile.getOriginalPath(), localDateTime, metadata, gpsZoneExtractor);

			// if the extracted EXIF date is stored in UTC time, so we must convert it to local time with guessed offset
			// to get the correct true local time at guessed offset
			if (!extractedDate.localTime()) {
				console.verboseln("Converting video date from UTC to local time with offset: %s", zoneOffsetToUse);
				OffsetDateTime utcDateTime = localDateTime.atOffset(ZoneOffset.UTC);
				localDateTimeToUse = utcDateTime.withOffsetSameInstant(zoneOffsetToUse).toLocalDateTime();
			}
		}

		MediaDateTime mediaDateTime = new MediaDateTime(localDateTimeToUse, zoneOffsetToUse);
		console.verboseln("Resolved create date: %s", mediaDateTime);
		return new AnalyzedMediaFile(mediaFile.getOriginalPath(), mediaFile.getMediaType(), metadata, mediaDateTime);
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
		String timeZone = configService.getTimeZone();
		ZoneId timeZoneId = (timeZone == null || timeZone.isEmpty()) ? null : ZoneId.of(timeZone);

		if (timeZoneId != null) {
			console.verboseln("Resolving offset by configured time zone: %s", timeZone);
		} else {
			console.verboseln("Resolving offset by system default time zone: %s", ZoneId.systemDefault());
		}

		return DateTimeUtils.getDefaultZoneOffset(localDateTime, timeZoneId);
	}

}
