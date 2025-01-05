package sk.kubisoft.exifsort.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.kubisoft.exifsort.MediaDateTime;
import sk.kubisoft.exifsort.MediaFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaFileSorterTest {

    private MediaFileSorter sorter;
    private Path sourceRoot;
    private Path targetRoot;

    @BeforeEach
    void setUp() {
        sorter = new MediaFileSorter();
        sourceRoot = Path.of("/source");
        targetRoot = Path.of("/target");
    }

    @Test
    void testSortSingleFile() {
        // Given
        var mediaFile = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null);
        var dateTime = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(mediaFile, dateTime);

        // When
        Map<Path, Path> result = sorter.sort(input, targetRoot);

        // Then
        assertEquals(1, result.size());
        assertEquals(
                targetRoot.resolve("2024/07/IMG7277.JPG"),
                result.get(sourceRoot.resolve("IMG7277.JPG"))
        );
    }

    @Test
    void testSortMultipleFilesFromSameMonth() {
        // Given
        var file1 = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null);
        var file2 = new MediaFile(sourceRoot.resolve("IMG7278.JPG"), null, null);
        var date = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(file1, date);
        input.put(file2, date);

        // When
        Map<Path, Path> result = sorter.sort(input, targetRoot);

        // Then
        assertEquals(2, result.size());
        assertEquals(
                targetRoot.resolve("2024/07/IMG7277.JPG"),
                result.get(sourceRoot.resolve("IMG7277.JPG"))
        );
        assertEquals(
                targetRoot.resolve("2024/07/IMG7278.JPG"),
                result.get(sourceRoot.resolve("IMG7278.JPG"))
        );
    }

    @Test
    void testSortFilesFromDifferentMonths() {
        // Given
        var julyFile = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null);
        var augustFile = new MediaFile(sourceRoot.resolve("IMG7278.JPG"), null, null);
        var julyDate = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));
        var augustDate = new MediaDateTime(LocalDateTime.of(2024, 8, 1, 10, 30));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(julyFile, julyDate);
        input.put(augustFile, augustDate);

        // When
        Map<Path, Path> result = sorter.sort(input, targetRoot);

        // Then
        assertEquals(2, result.size());
        assertEquals(
                targetRoot.resolve("2024/07/IMG7277.JPG"),
                result.get(sourceRoot.resolve("IMG7277.JPG"))
        );
        assertEquals(
                targetRoot.resolve("2024/08/IMG7278.JPG"),
                result.get(sourceRoot.resolve("IMG7278.JPG"))
        );
    }

    @Test
    void testSortFilesFromDifferentYears() {
        // Given
        var file2024 = new MediaFile(sourceRoot.resolve("IMG7277.JPG"), null, null);
        var file2023 = new MediaFile(sourceRoot.resolve("IMG7278.JPG"), null, null);
        var date2024 = new MediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30));
        var date2023 = new MediaDateTime(LocalDateTime.of(2023, 12, 31, 23, 59));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(file2024, date2024);
        input.put(file2023, date2023);

        // When
        Map<Path, Path> result = sorter.sort(input, targetRoot);

        // Then
        assertEquals(2, result.size());
        assertEquals(
                targetRoot.resolve("2024/07/IMG7277.JPG"),
                result.get(sourceRoot.resolve("IMG7277.JPG"))
        );
        assertEquals(
                targetRoot.resolve("2023/12/IMG7278.JPG"),
                result.get(sourceRoot.resolve("IMG7278.JPG"))
        );
    }

    @Test
    void testSortEmptyInput() {
        // Given
        Map<MediaFile, MediaDateTime> input = new HashMap<>();

        // When
        Map<Path, Path> result = sorter.sort(input, targetRoot);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testPreservesOriginalFileName() {
        // Given
        var complexFileName = new MediaFile(
                sourceRoot.resolve("IMG_20240115_123456_HDR.jpg"),
                null,
                null
        );
        var date = new MediaDateTime(LocalDateTime.of(2024, 1, 15, 12, 34, 56));

        Map<MediaFile, MediaDateTime> input = new HashMap<>();
        input.put(complexFileName, date);

        // When
        Map<Path, Path> result = sorter.sort(input, targetRoot);

        // Then
        assertEquals(
                targetRoot.resolve("2024/01/IMG_20240115_123456_HDR.jpg"),
                result.get(sourceRoot.resolve("IMG_20240115_123456_HDR.jpg"))
        );
    }
}