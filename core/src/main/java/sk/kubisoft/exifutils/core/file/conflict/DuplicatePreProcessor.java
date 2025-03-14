package sk.kubisoft.exifutils.core.file.conflict;

import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.MoveAction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.*;

@Singleton
public class DuplicatePreProcessor {

    private final FileExplorer fileExplorer;

    @Inject
    public DuplicatePreProcessor(FileExplorer fileExplorer) {
        this.fileExplorer = fileExplorer;
    }

    public List<MoveAction> processConflicts(List<MoveAction> moveActions) {
        List<MoveAction> result = new ArrayList<>();
        Map<Path, Set<String>> existingFilesMap = new HashMap<>();

        for (MoveAction moveAction : moveActions) {
            var targetPath = moveAction.target();
            var targetParent = targetPath.getParent();
            var targetFileName = targetPath.getFileName().toString();

            // 1. Get existing files in the target folder
            var existingFilesTargetFolder = existingFilesMap.computeIfAbsent(targetParent, k -> {
                var listedFiles = fileExplorer.listFiles(targetParent);
                return new HashSet<>(listedFiles.stream()
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .toList());
            });

            // 2. Take out the file name from existing files if the source is in the same directory as target
            var sourcePath = moveAction.source();
            var sourceParent = sourcePath.getParent();
            if (Objects.equals(sourceParent, targetParent)) {
                existingFilesTargetFolder.remove(sourcePath.getFileName().toString());
            }

            // 3. Check if the target file already exists
            if (existingFilesTargetFolder.contains(targetFileName)) {
                // 3a. Resolve conflict by renaming the file
                Path newFilePath = resolveFileNameConflict(targetParent, existingFilesTargetFolder, targetFileName);
                result.add(new MoveAction(moveAction.source(), newFilePath));
                existingFilesTargetFolder.add(newFilePath.getFileName().toString());
            } else {
                // 3b. Add the move action to the result
                result.add(moveAction);
                existingFilesTargetFolder.add(targetFileName);
            }
        }

        return result;
    }

    private Path resolveFileNameConflict(Path parentDir, Set<String> existingFilesTargetFolder, String fileName) {
        // If not identical, find a new name
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        int counter = 1;
        String newFileName;
        do {
            newFileName = String.format("%s_%d%s", baseName, counter++, extension);
        } while (existingFilesTargetFolder.contains(newFileName));

        return parentDir.resolve(newFileName);
    }

}
