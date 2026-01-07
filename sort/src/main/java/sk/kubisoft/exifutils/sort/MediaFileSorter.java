package sk.kubisoft.exifutils.sort;

import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.utils.DatePatternResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MediaFileSorter {

    private final MediaFileNameUtils fileNameUtils;

    private final DuplicatePreProcessor duplicatePreProcessor;

    private final ConfigService configService;

    @Inject
    public MediaFileSorter(MediaFileNameUtils fileNameUtils, DuplicatePreProcessor duplicatePreProcessor,
                           ConfigService configService) {
        this.fileNameUtils = fileNameUtils;
        this.duplicatePreProcessor = duplicatePreProcessor;
        this.configService = configService;
    }

    public List<MoveAction> sort(List<AnalyzedMediaFile> mediaFiles, Path targetRootPath, boolean rename,
                                 String sortPattern) {
        var effectivePattern = sortPattern != null ? sortPattern : configService.getSortPattern();
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
            var targetDateFolder = createTargetDateFolder(mediaFile.getCreationDate(), effectivePattern);
            var finalTargetPath = targetRootPath.resolve(targetDateFolder).resolve(targetFileName);

            moveActions.add(new MoveAction(originalPath, finalTargetPath));
        }
        moveActions.sort(MoveAction::compareTo);

        return duplicatePreProcessor.processConflicts(moveActions);
    }

    private Path createTargetDateFolder(MediaDateTime date, String pattern) {
        var localDateTime = date.getLocalDateTime();
        String resolved = DatePatternResolver.resolve(pattern, localDateTime);
        return Path.of(resolved);
    }

}
