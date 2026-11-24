package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNameAnalyzerTest {

    private final FileNameAnalyzer analyzer = new FileNameAnalyzer();

    @Test
    void shouldParseBasicIsoFormat() {
        var result = analyzer.analyzeFileName("20240122_153010_something.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseBasicFormat1() {
        var result = analyzer.analyzeFileName("2016-07-09 22-25-48.mp4");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2016, 7, 9, 22, 25, 48),
                date
        );
    }

    @Test
    void shouldParseIsoDateWithUnderscoreAndCompactTime() {
        var result = analyzer.analyzeFileName("2024-01-22_153010.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseIsoDateWithSpaceAndCompactTime() {
        var result = analyzer.analyzeFileName("2024-01-22 153010.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseBasicIsoFormatWithHyphen() {
        var result = analyzer.analyzeFileName("20240122-153010.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseImgFormat() {
        var result = analyzer.analyzeFileName("IMG_20240122_153010.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseVidFormat() {
        var result = analyzer.analyzeFileName("VID_20240122_153010.mp4");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseIphoneFormat() {
        var result = analyzer.analyzeFileName("24-01-22 15-30-10 4968.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldParseOlderFormat() {
        var result = analyzer.analyzeFileName("IMG_2008_01_17_11_13_00.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2008, 1, 17, 11, 13, 0),
                date
        );
    }

    @Test
    void shouldReturnEmptyForInvalidFormat() {
        var result = analyzer.analyzeFileName("invalid_filename.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyForNullInput() {
        var result = analyzer.analyzeFileName(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {
        var result = analyzer.analyzeFileName("");

        assertTrue(result.isEmpty());
    }

    // User-supplied pattern tests

    @Test
    void shouldParseWithUserSuppliedPattern() {
        var result = analyzer.analyzeFileName("photo_2024-01-22_15-30-10.jpg", "'photo_'yyyy-MM-dd'_'HH-mm-ss");

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 1, 22, 15, 30, 10), result.get());
    }

    @Test
    void shouldParseWithSimpleUserPattern() {
        var result = analyzer.analyzeFileName("20240122153010_extra.jpg", "yyyyMMddHHmmss");

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 1, 22, 15, 30, 10), result.get());
    }

    @Test
    void shouldReturnEmptyWhenUserPatternDoesNotMatch() {
        // User pattern is strict - no fallback to defaults
        var result = analyzer.analyzeFileName("IMG_20240122_153010.jpg", "'photo_'yyyy-MM-dd");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUseDefaultPatternsWhenUserPatternIsNull() {
        var result = analyzer.analyzeFileName("IMG_20240122_153010.jpg", null);

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 1, 22, 15, 30, 10), result.get());
    }

    @Test
    void shouldUseDefaultPatternsWhenUserPatternIsBlank() {
        var result = analyzer.analyzeFileName("IMG_20240122_153010.jpg", "   ");

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 1, 22, 15, 30, 10), result.get());
    }

    @Test
    void shouldHandleExtraCharactersAfterDateTime() {
        var result = analyzer.analyzeFileName("IMG_20240122_153010_HDR_BURST1.jpg");

        assertTrue(result.isPresent());
        var date = result.get();
        assertEquals(
                LocalDateTime.of(2024, 1, 22, 15, 30, 10),
                date
        );
    }

    @Test
    void shouldResolveDateOnlyUserPatternToMidnight() {
        var result = analyzer.analyzeFileName("2023-08-15-photo.jpg", "yyyy-MM-dd");

        assertEquals(Optional.of(LocalDateTime.of(2023, 8, 15, 0, 0, 0)), result);
    }

    @Test
    void shouldParseUserPatternWithoutSeconds() {
        var result = analyzer.analyzeFileName("20230815_1430.jpg", "yyyyMMdd_HHmm");

        assertEquals(Optional.of(LocalDateTime.of(2023, 8, 15, 14, 30, 0)), result);
    }

    @Test
    void shouldParseUserPatternWithClockHour() {
        var result = analyzer.analyzeFileName("20230815_143022.jpg", "yyyyMMdd_kkmmss");

        assertEquals(Optional.of(LocalDateTime.of(2023, 8, 15, 14, 30, 22)), result);
    }

    @Test
    void shouldNotDefaultHourWhenUserPatternHasIncompleteTime() {
        assertTrue(analyzer.analyzeFileName("20230815_3022.jpg", "yyyyMMdd_mmss").isEmpty());
        assertTrue(analyzer.analyzeFileName("20230815_023022.jpg", "yyyyMMdd_hhmmss").isEmpty());
    }

    @Test
    void shouldNotMatchUserPatternWithoutDate() {
        assertTrue(analyzer.analyzeFileName("143022.jpg", "HHmmss").isEmpty());
    }
}
