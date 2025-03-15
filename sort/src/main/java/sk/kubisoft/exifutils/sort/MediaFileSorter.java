package sk.kubisoft.exifutils.sort;

import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MediaFileSorter {

    private final MediaFileNameUtils fileNameUtils;

    private final DuplicatePreProcessor duplicatePreProcessor;

    @Inject
    public MediaFileSorter(MediaFileNameUtils fileNameUtils, DuplicatePreProcessor duplicatePreProcessor) {
        this.fileNameUtils = fileNameUtils;
        this.duplicatePreProcessor = duplicatePreProcessor;
    }

    public List<MoveAction> sort(List<AnalyzedMediaFile> mediaFiles, Path targetRootPath, boolean rename) {
        var moveActions = new ArrayList<MoveAction>();

        for (var mediaFile : mediaFiles) {
            var originalPath = mediaFile.getOriginalPath();
            var originalFileName = originalPath.getFileName().toString();

            String targetFileName;
            if (rename) {
                targetFileName = fileNameUtils.createNewName(mediaFile);
            } else {
                targetFileName = originalFileName;
            }
            var targetDateFolder = createTargetDateFolder(mediaFile.getCreationDate());
            var finalTargetPath = targetRootPath.resolve(targetDateFolder).resolve(targetFileName);

            moveActions.add(new MoveAction(originalPath, finalTargetPath));
        }
        moveActions.sort(MoveAction::compareTo);

        return duplicatePreProcessor.processConflicts(moveActions);
    }

    private Path createTargetDateFolder(MediaDateTime date) {
        var localDate = date.getLocalDateTime();

        var year = String.valueOf(localDate.getYear());
        var month = String.valueOf(localDate.getMonthValue());
        var paddedMonth = month.length() == 1 ? "0" + month : month;

        return Path.of(year, paddedMonth);
    }

}
