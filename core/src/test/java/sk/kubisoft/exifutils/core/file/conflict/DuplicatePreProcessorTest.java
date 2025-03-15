package sk.kubisoft.exifutils.core.file.conflict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.MoveAction;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicatePreProcessorTest {

    @Mock
    private FileExplorer fileExplorerMock;

    private DuplicatePreProcessor duplicatePreProcessor;

    @BeforeEach
    void setUp() {
        duplicatePreProcessor = new DuplicatePreProcessor(fileExplorerMock);
    }

    @Test
    void testMoveWithoutConflictsToDifferentEmptyDir() {
        var moveActionList = List.of(
                new MoveAction(Path.of("dir1", "file1"), Path.of("dir2", "file1")),
                new MoveAction(Path.of("dir1", "file2"), Path.of("dir2", "file2")),
                new MoveAction(Path.of("dir1", "file3"), Path.of("dir2", "file3"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(3);
        // Unchanged list, no conflicts
        assertThat(result).containsExactlyElementsOf(moveActionList);
    }

    @Test
    void testMoveWithoutConflictsToSameEmptyDir() {
        var moveActionList = List.of(
                new MoveAction(Path.of("dir1", "file1"), Path.of("dir1", "file1")),
                new MoveAction(Path.of("dir1", "file2"), Path.of("dir1", "file2")),
                new MoveAction(Path.of("dir1", "file3"), Path.of("dir1", "file3"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(3);
        // Unchanged list, no conflicts
        assertThat(result).containsExactlyElementsOf(moveActionList);
    }

    @Test
    void testMoveWithConflictsToDifferentEmptyDir() {
        var moveActionList = List.of(
                new MoveAction(Path.of("dir1", "IMG_1024.jpg"), Path.of("dir2", "IMG_20250101_102030.jpg")),
                new MoveAction(Path.of("dir1", "IMG_1025.jpg"), Path.of("dir2", "IMG_20250101_102030.jpg"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(2);
        // First file is moved without conflict
        assertThat(result.getFirst().target()).isEqualTo(Path.of("dir2", "IMG_20250101_102030.jpg"));
        // Second file is moved with conflict resolved
        assertThat(result.getLast().target()).isEqualTo(Path.of("dir2", "IMG_20250101_102030_1.jpg"));
    }

    @Test
    void testMoveWithConflictsToDifferentDirWithExistingName() {
        when(fileExplorerMock.listFiles(Path.of("dir2")))
                .thenReturn(List.of(Path.of("IMG_20250101_102030.jpg")));

        var moveActionList = List.of(
                new MoveAction(Path.of("dir1", "IMG_1024.jpg"), Path.of("dir2", "IMG_20250101_102020.jpg")),
                new MoveAction(Path.of("dir1", "IMG_1025.jpg"), Path.of("dir2", "IMG_20250101_102030.jpg"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(2);
        // First file is moved without conflict
        assertThat(result.get(0).target()).isEqualTo(Path.of("dir2", "IMG_20250101_102020.jpg"));
        // Second file is moved with conflict resolved
        assertThat(result.get(1).target()).isEqualTo(Path.of("dir2", "IMG_20250101_102030_1.jpg"));
    }

    @Test
    void testMoveWithConflictsToSameDirWithExistingNames() {
        when(fileExplorerMock.listFiles(Path.of("dir")))
                .thenReturn(List.of(Path.of("IMG_20250101_102030.jpg"), Path.of("IMG_20250101_102031.jpg"), Path.of("IMG_20250101_102032.jpg")));

        var moveActionList = List.of(
                new MoveAction(Path.of("dir", "IMG_20250101_102030.jpg"), Path.of("dir", "IMG_20250101_102030.jpg")),
                new MoveAction(Path.of("dir", "IMG_20250101_102031.jpg"), Path.of("dir", "IMG_20250101_102031.jpg")),
                new MoveAction(Path.of("dir", "IMG_20250101_102032.jpg"), Path.of("dir", "IMG_20250101_102032.jpg"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(3);
        // All files are moved without conflict, because they are already in the target directory with the same name
        assertThat(result.get(0).target()).isEqualTo(Path.of("dir", "IMG_20250101_102030.jpg"));
        assertThat(result.get(1).target()).isEqualTo(Path.of("dir", "IMG_20250101_102031.jpg"));
        assertThat(result.get(2).target()).isEqualTo(Path.of("dir", "IMG_20250101_102032.jpg"));
    }

    @Test
    void testMoveWithConflictsToTwoDifferentDirsWithConflicts() {
        when(fileExplorerMock.listFiles(Path.of("dir1")))
                .thenReturn(List.of(Path.of("IMG_20250101_102030.jpg"), Path.of("IMG_1024.jpg"), Path.of("IMG_1025.jpg")));
        when(fileExplorerMock.listFiles(Path.of("dir2")))
                .thenReturn(List.of(Path.of("IMG_20250101_102040.jpg")));

        var moveActionList = List.of(
                new MoveAction(Path.of("dir1", "IMG_1024.jpg"), Path.of("dir1", "IMG_20250101_102030.jpg")),
                new MoveAction(Path.of("dir1", "IMG_1025.jpg"), Path.of("dir2", "IMG_20250101_102040.jpg"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(2);
        // First file is moved with conflict resolved
        assertThat(result.get(0).target()).isEqualTo(Path.of("dir1", "IMG_20250101_102030_1.jpg"));
        // Second file is moved with conflict resolved
        assertThat(result.get(1).target()).isEqualTo(Path.of("dir2", "IMG_20250101_102040_1.jpg"));
    }

    @Test
    void testMoveWithConflictsInSameDirToSameName() {
        when(fileExplorerMock.listFiles(Path.of("dir")))
                .thenReturn(List.of(Path.of("IMG_20230804_174615.jpg"), Path.of("IMG_20230804_174615_1.jpg"), Path.of("IMG_20230804_174615_3.jpg")));

        var moveActionList = List.of(
                new MoveAction(Path.of("dir", "IMG_20230804_174615.jpg"), Path.of("dir", "IMG_20230804_174615.jpg")),
                new MoveAction(Path.of("dir", "IMG_20230804_174615_1.jpg"), Path.of("dir", "IMG_20230804_174615.jpg")),
                new MoveAction(Path.of("dir", "IMG_20230804_174615_3.jpg"), Path.of("dir", "IMG_20230804_174615.jpg"))
        );

        var result = duplicatePreProcessor.processConflicts(moveActionList);

        assertThat(result).hasSize(3);
        var first = result.get(0);
        assertThat(first.source()).isEqualTo(Path.of("dir", "IMG_20230804_174615.jpg"));
        assertThat(first.target()).isEqualTo(Path.of("dir", "IMG_20230804_174615.jpg"));
        var second = result.get(1);
        assertThat(second.source()).isEqualTo(Path.of("dir", "IMG_20230804_174615_1.jpg"));
        assertThat(second.target()).isEqualTo(Path.of("dir", "IMG_20230804_174615_1.jpg"));
        var third = result.get(2);
        assertThat(third.source()).isEqualTo(Path.of("dir", "IMG_20230804_174615_3.jpg"));
        assertThat(third.target()).isEqualTo(Path.of("dir", "IMG_20230804_174615_2.jpg"));
    }
}