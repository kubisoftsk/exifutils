package sk.kubisoft.exifsort;

import java.nio.file.Path;
import java.util.List;

public record ExifSortApplicationInput(

		List<Path> sourceDirectories,

		Path destinationDirectory,

		boolean dryRun,

		boolean verbose

) {

}
