package sk.kubisoft.exifsort.config;

import sk.kubisoft.exifsort.MediaDateTime;
import sk.kubisoft.exifsort.MediaFile;

import java.nio.file.Path;
import java.util.Map;

public class MediaFileSorter {

    public Map<Path, Path> sort(Map<MediaFile, MediaDateTime> mediaFilesWithDate, Path targetRootPath) {
        Map<Path, Path> moveActions = new java.util.LinkedHashMap<>();

        for (var entry : mediaFilesWithDate.entrySet()) {
            var mediaFile = entry.getKey();
            var date = entry.getValue();

            var originalPath = mediaFile.originalPath();
            var originalFileName = originalPath.getFileName().toString();

            var targetDateFolder = createTargetDateFolder(date);
            var finalTargetPath = targetRootPath.resolve(targetDateFolder).resolve(originalFileName);

            moveActions.put(originalPath, finalTargetPath);
        }

        return moveActions;
    }

    private Path createTargetDateFolder(MediaDateTime date) {
        var localDate = date.getLocalDateTime();

        var year = String.valueOf(localDate.getYear());
        var month = String.valueOf(localDate.getMonthValue());
        var paddedMonth = month.length() == 1 ? "0" + month : month;

        return Path.of(year, paddedMonth);
    }

}
