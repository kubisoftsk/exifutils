package sk.kubisoft.exifsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;

public class ExifSort {

	private static final Logger logger = LoggerFactory.getLogger(ExifSort.class);

	private final FileExplorer fileExplorer = new FileExplorer();

	private final MediaDateExtractor mediaDateExtractor = new MediaDateExtractor();

	public void run(ExifSortApplicationInput input) {
		logger.info("Running ExifSort with input: {}", input);

		var files = fileExplorer.listFiles(input.sourceDirectories());
		logger.info("Found {} files", files.size());

		for (var file : files) {
			try {
				var instant = mediaDateExtractor.extractCreationDate(file);
				logger.info("Processing file: {}: {}", file, instant.atZone(ZoneId.of("Europe/Bratislava")));
			} catch (Exception e) {
				logger.error("An error occurred while processing file: " + file, e);
			}
		}
	}
}
