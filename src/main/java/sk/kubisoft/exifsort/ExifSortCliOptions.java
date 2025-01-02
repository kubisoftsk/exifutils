package sk.kubisoft.exifsort;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

final class ExifSortCliOptions {

	/*
	 * NAME
	 *     exifsort - sort files based on EXIF metadata
	 *
	 * SYNOPSIS
	 *     exifsort [-v] [-n] -d DIR [DIR...]
	 *
	 * DESCRIPTION
	 *     Sort files based on their EXIF metadata into organized directory structures.
	 *
	 * OPTIONS
	 *     -d, --destination=DIR
	 *             Destination root directory for the sorted files.
	 *     -n, --dry-run
	 *             Show what would be done, without making any changes.
	 *     -v, --verbose
	 *             Print verbose output.
	 *
	 * ARGUMENTS
	 *     DIR...  Source directories to scan recursively for files to sort.
	 *             If not specified, the current directory is used.
	 */

	private ExifSortCliOptions() {
		throw new UnsupportedOperationException();
	}

	public static final String USAGE = "exifsort [-v] [-n] -d DIR [DIR...]";
	public static final Options EXIF_SORT_OPTIONS = new Options();

	public static final Option DESTINATION = Option.builder()
												   .option("d").hasArg()
												   .longOpt("destination").hasArg().argName("DIR")
												   .required()
												   .desc("Destination root directory for the sorted files.")
												   .build();

	public static final Option DRY_RUN = Option.builder()
											   .option("n")
											   .longOpt("dry-run")
											   .desc("Show what would be done, without making any changes.")
											   .build();

	public static final Option VERBOSE = Option.builder()
											   .option("v")
											   .longOpt("verbose")
											   .desc("Print verbose output.")
											   .build();

	static {
		EXIF_SORT_OPTIONS.addOption(DESTINATION);
		EXIF_SORT_OPTIONS.addOption(DRY_RUN);
		EXIF_SORT_OPTIONS.addOption(VERBOSE);
	}
}
