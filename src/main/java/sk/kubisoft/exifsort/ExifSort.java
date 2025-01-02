package sk.kubisoft.exifsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExifSort {

	private static final Logger logger = LoggerFactory.getLogger(ExifSort.class);

	public void run(ExifSortApplicationInput input) {
		logger.info("Running ExifSort with input: {}", input);
	}
}
