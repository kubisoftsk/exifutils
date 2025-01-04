package sk.kubisoft.exifsort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DateTimeException;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class ExifDateParserTest {

    @Test
    void testCorrectDateWithoutOffset() {
        String dateStr = "2023:08:31 15:10:31";
        String offsetStr = null;

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31Z", dateTime.get().toString());
    }

    @Test
    void testCorrectDateWithEmbeddedOffset() {
        String dateStr = "2023:08:31 15:10:31+03:00";
        String offsetStr = null;

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31+03:00", dateTime.get().toString());
    }

    @Test
    void testCorrectDateWithProvidedOffset() {
        String dateStr = "2023:08:31 15:10:31";
        String offsetStr = "+03:00";

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31+03:00", dateTime.get().toString());
    }

    @Test
    void testDateWithFractionalSeconds() {
        String dateStr = "2023:08:31 15:10:31.123";
        String offsetStr = null;

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31Z", dateTime.get().toString());
    }

    @Test
    void testDateWithOffsetWithoutColon() {
        String dateStr = "2023:08:31 15:10:31+0300";
        String offsetStr = null;

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31+03:00", dateTime.get().toString());
    }

    @Test
    void testEmbeddedOffsetTakesPrecedenceOverProvidedOffset() {
        String dateStr = "2023:08:31 15:10:31+03:00";
        String offsetStr = "+02:00";

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31+03:00", dateTime.get().toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "0000:00:00 00:00:00"})
    void testInvalidOrBlankDates(String dateStr) {
        var dateTime = ExifDateParser.parseExifDate(dateStr, null);
        assertTrue(dateTime.isEmpty());
    }

    @Test
    void testInvalidDateFormat() {
        String dateStr = "2023-08-31 15:10:31"; // uses hyphen instead of colon

        assertThrows(DateTimeParseException.class, () ->
                ExifDateParser.parseExifDate(dateStr, null)
        );
    }

    @Test
    void testInvalidOffsetFormat() {
        String dateStr = "2023:08:31 15:10:31";
        String offsetStr = "invalid";

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        // Should fallback to UTC when offset is invalid
        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31Z", dateTime.get().toString());
    }

    @Test
    void testDateWithZuluTimezone() {
        String dateStr = "2023:08:31 15:10:31Z";
        String offsetStr = null;

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31Z", dateTime.get().toString());
    }

    @Test
    void testDateWithNegativeOffset() {
        String dateStr = "2023:08:31 15:10:31-0500";
        String offsetStr = null;

        var dateTime = ExifDateParser.parseExifDate(dateStr, offsetStr);

        assertTrue(dateTime.isPresent());
        assertEquals("2023-08-31T15:10:31-05:00", dateTime.get().toString());
    }
}