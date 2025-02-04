package sk.kubisoft.exifutils.core.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExifDateParserTest {

    private final ExifDateParser exifDateParser = new ExifDateParser();

    @Test
    void testCorrectDateWithoutOffset() {
        String dateStr = "2023:08:31 15:10:31";
        String offsetStr = null;

        var dateTimeOptional = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTimeOptional).isPresent();
        var dateTime = dateTimeOptional.get();
        assertThat(dateTime.localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.zoneOffset()).isNull();
    }

    @Test
    void testCorrectDateWithEmbeddedOffset() {
        String dateStr = "2023:08:31 15:10:31+03:00";
        String offsetStr = null;

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void testCorrectDateWithProvidedOffset() {
        String dateStr = "2023:08:31 15:10:31";
        String offsetStr = "+03:00";

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void testDateWithFractionalSeconds() {
        String dateStr = "2023:08:31 15:10:31.123";
        String offsetStr = null;

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset()).isNull();
    }

    @Test
    void testDateWithOffsetWithoutColon() {
        String dateStr = "2023:08:31 15:10:31+0300";
        String offsetStr = null;

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void testEmbeddedOffsetTakesPrecedenceOverProvidedOffset() {
        String dateStr = "2023:08:31 15:10:31+03:00";
        String offsetStr = "+02:00";

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset().toString()).isEqualTo("+03:00");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "0000:00:00 00:00:00"})
    void testInvalidOrBlankDates(String dateStr) {
        var dateTime = exifDateParser.parseExifDate(dateStr, null);
        assertThat(dateTime).isEmpty();
    }

    @Test
    void testInvalidDateFormat() {
        String dateStr = "2023-08-31 15:10:31"; // uses hyphen instead of colon

        assertThrows(DateTimeParseException.class, () ->
                exifDateParser.parseExifDate(dateStr, null)
        );
    }

    @Test
    void testInvalidOffsetFormat() {
        String dateStr = "2023:08:31 15:10:31";
        String offsetStr = "invalid";

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset()).isNull();
    }

    @Test
    void testDateWithNegativeOffset() {
        String dateStr = "2023:08:31 15:10:31-0500";
        String offsetStr = null;

        var dateTime = exifDateParser.parseExifDate(dateStr, offsetStr);

        assertThat(dateTime).isPresent();
        assertThat(dateTime.get().localDateTime()).isEqualTo("2023-08-31T15:10:31");
        assertThat(dateTime.get().zoneOffset().toString()).isEqualTo("-05:00");
    }
}