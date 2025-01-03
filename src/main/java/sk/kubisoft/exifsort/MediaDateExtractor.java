package sk.kubisoft.exifsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
import sk.kubisoft.exifsort.config.ConfigService;

public class MediaDateExtractor {

	private static final Logger logger = LoggerFactory.getLogger(MediaDateExtractor.class);

	private final ConfigService configService = ConfigService.getInstance();
	private final ExifTool exifTool;
	private static final DateTimeFormatter EXIF_DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

	public MediaDateExtractor() {
		var exifToolConfig = configService.getConfig().getExifTool();
		if (exifToolConfig == null || exifToolConfig.getPath() == null) {
			throw new IllegalArgumentException("ExifTool path not configured");
		}
		this.exifTool = new ExifToolBuilder()
				.withPath(exifToolConfig.getPath())
				.enableStayOpen()  // Performance optimization for multiple files
				.build();
	}

	public Instant extractCreationDate(Path file) throws Exception {
		try {
			var metadata = exifTool.getImageMeta(file.toFile());

			// Order of precedence for date fields
			String[] dateFields = {
					"DateTimeOriginal",     // Standard Exif date
					"CreateDate",           // General creation date
					"MediaCreateDate",      // For videos
					"TrackCreateDate",      // For videos
					"ModifyDate"            // Last resort
			};

			for (String field : dateFields) {
				String dateStr = metadata.get(new UnspecifiedTag(field));
				if (dateStr != null && !dateStr.trim().isEmpty()) {
					try {
						return parseExifDate(dateStr);
					} catch (Exception e) {
						// Log and continue to next field
						logger.warn("Could not parse date from {} field: {}", field, dateStr, e);
					}
				}
			}

			// Fallback to file system date if no EXIF date found
			logger.info("No EXIF date found for {}, using file modification time", file);
			return Files.getLastModifiedTime(file).toInstant();

		} catch (Exception e) {
			throw new Exception("Failed to read media date from " + file + ": " + e.getMessage(), e);
		}
	}

	private Instant parseExifDate(String dateStr) {
		// Clean up the date string (some cameras add timezone or fractional seconds)
		dateStr = dateStr.split("\\+")[0].split("\\.")[0].trim();
		LocalDateTime dateTime = LocalDateTime.parse(dateStr, EXIF_DATE_FORMAT);
		return dateTime.atZone(ZoneId.systemDefault()).toInstant();
	}

	public void close() {
		if (exifTool != null) {
			try {
				exifTool.close();
			} catch (Exception e) {
				logger.error("Error closing ExifTool", e);
			}
		}
	}
}