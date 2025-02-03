package sk.kubisoft.exifutils.sort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MediaFileSorterTest {

    @Mock
    private MediaFileNameUtils fileNameUtils;
    
    private MediaFileSorter sorter;
    private Path sourceRoot;
    private Path targetRoot;

    @BeforeEach
    void setUp() {
        sorter = new MediaFileSorter(fileNameUtils);
        sourceRoot = Path.of("/source");
        targetRoot = Path.of("/target");
    }

    @Test
    void testSortSingleFile() {
        // Given
        var mediaFile = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null, null);
        var dateTime = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(mediaFile, dateTime);

        // When
        List<MoveAction> result = sorter.sort(input, targetRoot, false);

        // Then
        assertEquals(1, result.size());
        var moveAction = result.get(0);
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction.target());
    }

    @Test
    void testSortMultipleFilesFromSameMonth() {
        // Given
        var file1 = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null, null);
        var file2 = new MediaFile(sourceRoot.resolve("IMG7278.JPG"), null, null, null);
        var date = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(file1, date);
        input.put(file2, date);

        // When
        List<MoveAction> result = sorter.sort(input, targetRoot, false);

        // Then
        assertEquals(2, result.size());
        var moveAction1 = result.get(0);
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction1.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction1.target());
        var moveAction2 = result.get(1);
        assertEquals(sourceRoot.resolve("IMG7278.JPG"), moveAction2.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7278.JPG"), moveAction2.target());
    }

    @Test
    void testSortFilesFromDifferentMonths() {
        // Given
        var julyFile = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null, null);
        var augustFile = new MediaFile(sourceRoot.resolve("IMG7278.JPG"), null, null, null);
        var julyDate = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));
        var augustDate = new MediaDateTime(LocalDateTime.of(2024, 8, 1, 10, 30));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(julyFile, julyDate);
        input.put(augustFile, augustDate);

        // When
        List<MoveAction> result = sorter.sort(input, targetRoot, false);

        // Then
        assertEquals(2, result.size());
        var moveAction1 = result.get(0);
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction1.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction1.target());
        var moveAction2 = result.get(1);
        assertEquals(sourceRoot.resolve("IMG7278.JPG"), moveAction2.source());
        assertEquals(targetRoot.resolve("2024/08/IMG7278.JPG"), moveAction2.target());
    }

    @Test
    void testSortFilesFromDifferentYears() {
        // Given
        var file2024 = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null, null);
        var file2023 = new MediaFile(sourceRoot.resolve("IMG7278.JPG"), null, null, null);
        var date2024 = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));
        var date2023 = new MediaDateTime(LocalDateTime.of(2023, 12, 31, 23, 59));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(file2024, date2024);
        input.put(file2023, date2023);

        // When
        List<MoveAction> result = sorter.sort(input, targetRoot, false);

        // Then
        assertEquals(2, result.size());
        var moveAction1 = result.get(0);
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction1.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction1.target());
        var moveAction2 = result.get(1);
        assertEquals(sourceRoot.resolve("IMG7278.JPG"), moveAction2.source());
        assertEquals(targetRoot.resolve("2023/12/IMG7278.JPG"), moveAction2.target());
    }

    @Test
    void testSortEmptyInput() {
        // Given
        Map<MediaFile, MediaDateTime> input = new HashMap<>();

        // When
        List<MoveAction> result = sorter.sort(input, targetRoot, false);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testPreservesOriginalFileName() {
        // Given
        var complexFileName = new MediaFile(
                sourceRoot.resolve("IMG_20240115_123456_HDR.jpg"),
                null,
                null,
                null
        );
        var date = new MediaDateTime(LocalDateTime.of(2024, 1, 15, 12, 34, 56));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(complexFileName, date);

        // When
        List<MoveAction> result = sorter.sort(input, targetRoot, false);

        // Then
        var moveAction = result.get(0);
        assertEquals(sourceRoot.resolve("IMG_20240115_123456_HDR.jpg"), moveAction.source());
        assertEquals(targetRoot.resolve("2024/01/IMG_20240115_123456_HDR.jpg"), moveAction.target());
    }
}