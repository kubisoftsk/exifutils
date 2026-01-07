package sk.kubisoft.exifutils.sort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.MoveAction;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static sk.kubisoft.exifutils.core.media.MediaType.IMAGE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaFileSorterTest {

    private static final String DEFAULT_PATTERN = "${date,yyyy}/${date,MM}";

    @Mock
    private MediaFileNameUtils fileNameUtils;

    @Mock
    private DuplicatePreProcessor duplicatePreProcessor;

    @Mock
    private ConfigService configService;

    private MediaFileSorter sorter;
    private Path sourceRoot;
    private Path targetRoot;

    @BeforeEach
    void setUp() {
        when(duplicatePreProcessor.processConflicts(anyList()))
                // Return the same list of actions without any processing for this test
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(configService.getSortPattern()).thenReturn(DEFAULT_PATTERN);

        sorter = new MediaFileSorter(fileNameUtils, duplicatePreProcessor, configService);
        sourceRoot = Path.of("/source");
        targetRoot = Path.of("/target");
    }

    @Test
    void testSortSingleFile() {
        // Given
        var mediaFile = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));

        // When - null pattern uses default from config
        List<MoveAction> result = sorter.sort(List.of(mediaFile), targetRoot, false, null);

        // Then
        assertEquals(1, result.size());
        var moveAction = result.getFirst();
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction.target());
    }

    @Test
    void testSortMultipleFilesFromSameMonth() {
        // Given
        var file1 = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));
        var file2 = new AnalyzedMediaFile(sourceRoot.resolve("IMG7278.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));

        // When
        List<MoveAction> result = sorter.sort(List.of(file1, file2), targetRoot, false, null);

        // Then
        assertEquals(2, result.size());
        var moveAction1 = result.getFirst();
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction1.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction1.target());
        var moveAction2 = result.getLast();
        assertEquals(sourceRoot.resolve("IMG7278.JPG"), moveAction2.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7278.JPG"), moveAction2.target());
    }

    @Test
    void testSortFilesFromDifferentMonths() {
        // Given
        var julyFile = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));
        var augustFile = new AnalyzedMediaFile(sourceRoot.resolve("IMG7278.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 8, 1, 10, 30)));

        // When
        List<MoveAction> result = sorter.sort(List.of(julyFile, augustFile), targetRoot, false, null);

        // Then
        assertEquals(2, result.size());
        var moveAction1 = result.getFirst();
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction1.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction1.target());
        var moveAction2 = result.getLast();
        assertEquals(sourceRoot.resolve("IMG7278.JPG"), moveAction2.source());
        assertEquals(targetRoot.resolve("2024/08/IMG7278.JPG"), moveAction2.target());
    }

    @Test
    void testSortFilesFromDifferentYears() {
        // Given
        var file2024 = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));
        var file2023 = new AnalyzedMediaFile(sourceRoot.resolve("IMG7278.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2023, 12, 31, 23, 59)));

        // When
        List<MoveAction> result = sorter.sort(List.of(file2024, file2023), targetRoot, false, null);

        // Then
        assertEquals(2, result.size());
        var moveAction1 = result.getFirst();
        assertEquals(sourceRoot.resolve("IMG7277.JPG"), moveAction1.source());
        assertEquals(targetRoot.resolve("2024/07/IMG7277.JPG"), moveAction1.target());
        var moveAction2 = result.getLast();
        assertEquals(sourceRoot.resolve("IMG7278.JPG"), moveAction2.source());
        assertEquals(targetRoot.resolve("2023/12/IMG7278.JPG"), moveAction2.target());
    }

    @Test
    void testPreservesOriginalFileName() {
        // Given
        var complexFileName = new AnalyzedMediaFile(sourceRoot.resolve("IMG_20240115_123456_HDR.jpg"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 1, 15, 12, 34, 56)));

        // When
        List<MoveAction> result = sorter.sort(List.of(complexFileName), targetRoot, false, null);

        // Then
        var moveAction = result.getFirst();
        assertEquals(sourceRoot.resolve("IMG_20240115_123456_HDR.jpg"), moveAction.source());
        assertEquals(targetRoot.resolve("2024/01/IMG_20240115_123456_HDR.jpg"), moveAction.target());
    }

    @Test
    void testCustomPatternOverridesDefault() {
        // Given
        var mediaFile = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));
        String customPattern = "${date,yyyy}/${date,MM}/${date,dd}";

        // When - custom pattern is provided
        List<MoveAction> result = sorter.sort(List.of(mediaFile), targetRoot, false, customPattern);

        // Then
        var moveAction = result.getFirst();
        assertEquals(targetRoot.resolve("2024/07/15/IMG7277.JPG"), moveAction.target());
    }

    @Test
    void testFlatPatternStructure() {
        // Given
        var mediaFile = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));
        String flatPattern = "${date,yyyy-MM}";

        // When
        List<MoveAction> result = sorter.sort(List.of(mediaFile), targetRoot, false, flatPattern);

        // Then
        var moveAction = result.getFirst();
        assertEquals(targetRoot.resolve("2024-07/IMG7277.JPG"), moveAction.target());
    }

    @Test
    void testWeekBasedPattern() {
        // Given
        var mediaFile = new AnalyzedMediaFile(sourceRoot.resolve("IMG7277.JPG"), IMAGE, emptyMap(),
                mediaDateTime(LocalDateTime.of(2024, 7, 15, 10, 30)));
        String weekPattern = "${date,yyyy}/W${date,ww}";

        // When
        List<MoveAction> result = sorter.sort(List.of(mediaFile), targetRoot, false, weekPattern);

        // Then
        var moveAction = result.getFirst();
        assertEquals(targetRoot.resolve("2024/W29/IMG7277.JPG"), moveAction.target());
    }

    private MediaDateTime mediaDateTime(LocalDateTime localDateTime) {
        return new MediaDateTime(localDateTime, ZoneOffset.UTC);
    }
}