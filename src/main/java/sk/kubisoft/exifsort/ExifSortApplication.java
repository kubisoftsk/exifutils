package sk.kubisoft.exifsort;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sk.kubisoft.exifsort.ExifSortCliOptions.EXIF_SORT_OPTIONS;
import static sk.kubisoft.exifsort.ExifSortCliOptions.USAGE;

public class ExifSortApplication {

	private static final Logger logger = LoggerFactory.getLogger(ExifSortApplication.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			printHelp(null);
			System.exit(64); // EX_USAGE in Unix/BSD - command line usage error
			return;
		}

		try {
			CommandLine cmd = new DefaultParser().parse(EXIF_SORT_OPTIONS, args);

			var input = parseInput(cmd);
			new ExifSort().run(input);
		} catch (ParseException e) {
			printHelp(e.getMessage());
			System.exit(64);
		} catch (RuntimeException e) {
			logger.error("An error occurred while executing the program", e);
			System.exit(70); // EX_SOFTWARE - internal software error
		}
	}

	private void run(ExifSortApplicationInput input) {

	}

	private static ExifSortApplicationInput parseInput(CommandLine cmd) throws ParseException {
		// Parse source directories - if none provided, use current directory
		List<Path> sourceDirs = new ArrayList<>();
		String[] args = cmd.getArgs();
		if (args.length == 0) {
			sourceDirs.add(Paths.get("."));
		} else {
			for (String sourceArg : args) {
				Path sourceDir = Paths.get(sourceArg);
				if (!Files.exists(sourceDir)) {
					throw new ParseException("Source directory does not exist: " + sourceArg);
				}
				if (!Files.isDirectory(sourceDir)) {
					throw new ParseException("Source path is not a directory: " + sourceArg);
				}
				if (!Files.isReadable(sourceDir)) {
					throw new ParseException("Cannot read source directory: " + sourceArg);
				}
				sourceDirs.add(sourceDir);
			}
		}

		// Parse destination directory (already validated as required by Options setup)
		String destPath = cmd.getOptionValue(ExifSortCliOptions.DESTINATION.getOpt());
		Path destinationDir = Paths.get(destPath);

		// Check if destination exists and is a directory
		// If not dry run, perform directory checks
		boolean dryRun = cmd.hasOption(ExifSortCliOptions.DRY_RUN.getOpt());
		if (!dryRun) {
			if (Files.exists(destinationDir)) {
				if (!Files.isDirectory(destinationDir)) {
					throw new ParseException("Destination path exists but is not a directory: " + destPath);
				}

				try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(destinationDir)) {
					if (dirStream.iterator().hasNext()) {
						throw new ParseException("Destination directory is not empty: " + destPath);
					}
				} catch (IOException e) {
					throw new ParseException("Cannot access destination directory: " + e.getMessage());
				}
			} else {
				try {
					Files.createDirectories(destinationDir);
				} catch (IOException e) {
					throw new ParseException("Cannot create destination directory: " + e.getMessage());
				}
			}
		}

		// Parse verbose flag
		boolean verbose = cmd.hasOption(ExifSortCliOptions.VERBOSE.getOpt());

		return new ExifSortApplicationInput(sourceDirs, destinationDir, dryRun, verbose);
	}

	private static void printHelp(String errorMessage) {
		if (StringUtils.isNotEmpty(errorMessage)) {
			System.err.println("Error: " + errorMessage);
			System.err.println();
		} else {
			System.out.println("ExifSort - sort files based on EXIF metadata");
			System.out.println();
		}

		var helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(USAGE, EXIF_SORT_OPTIONS);
	}
}
