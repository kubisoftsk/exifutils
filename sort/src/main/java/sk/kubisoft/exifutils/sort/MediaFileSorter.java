package sk.kubisoft.exifutils.sort;

import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class MediaFileSorter {

    private final MediaFileNameUtils fileNameUtils;

    @Inject
    public MediaFileSorter(MediaFileNameUtils fileNameUtils) {
        this.fileNameUtils = fileNameUtils;
    }

    public List<MoveAction> sort(Map<MediaFile, MediaDateTime> mediaFilesWithDate, Path targetRootPath, boolean rename) {
        var moveActions = new ArrayList<MoveAction>();

        for (var entry : mediaFilesWithDate.entrySet()) {
            var mediaFile = entry.getKey();
            var date = entry.getValue();

            var originalPath = mediaFile.originalPath();
            var originalFileName = originalPath.getFileName().toString();

            String targetFileName;
            if (rename) {
                targetFileName = fileNameUtils.createNewName(mediaFile, date);
            } else {
                targetFileName = originalFileName;
            }
            var targetDateFolder = createTargetDateFolder(date);
            var finalTargetPath = targetRootPath.resolve(targetDateFolder).resolve(targetFileName);

            moveActions.add(new MoveAction(originalPath, finalTargetPath));
        }
        moveActions.sort(MoveAction::compareTo);
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
