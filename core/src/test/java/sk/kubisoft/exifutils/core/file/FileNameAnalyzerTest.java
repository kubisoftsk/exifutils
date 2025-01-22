package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
}