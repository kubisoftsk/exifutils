package sk.kubisoft.exifutils.core.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatePatternResolverTest {

    private static final LocalDateTime TEST_DATE = LocalDateTime.of(2024, 7, 15, 10, 30, 45);

    @Test
    void testYearMonthPattern() {
        String result = DatePatternResolver.resolve("${date,yyyy}/${date,MM}", TEST_DATE);
        assertEquals("2024/07", result);
    }

    @Test
    void testYearMonthDayPattern() {
        String result = DatePatternResolver.resolve("${date,yyyy}/${date,MM}/${date,dd}", TEST_DATE);
        assertEquals("2024/07/15", result);
    }

    @Test
    void testWeekOfYearPattern() {
        String result = DatePatternResolver.resolve("${date,yyyy}/${date,ww}", TEST_DATE);
        assertEquals("2024/29", result);
    }

    @Test
    void testFlatYearMonthPattern() {
        String result = DatePatternResolver.resolve("${date,yyyy-MM}", TEST_DATE);
        assertEquals("2024-07", result);
    }

    @Test
    void testComplexPattern() {
        String result = DatePatternResolver.resolve("${date,yyyy}/${date,MM}/${date,dd}_${date,HHmmss}", TEST_DATE);
        assertEquals("2024/07/15_103045", result);
    }

    @Test
    void testPatternWithLiteralText() {
        String result = DatePatternResolver.resolve("photos/${date,yyyy}/month_${date,MM}", TEST_DATE);
        assertEquals("photos/2024/month_07", result);
    }

    @Test
    void testSingleDigitMonth() {
        LocalDateTime januaryDate = LocalDateTime.of(2024, 1, 5, 10, 30);
        String result = DatePatternResolver.resolve("${date,yyyy}/${date,MM}", januaryDate);
        assertEquals("2024/01", result);
    }

    @Test
    void testQuarterPattern() {
        String result = DatePatternResolver.resolve("${date,yyyy}/Q${date,Q}", TEST_DATE);
        assertEquals("2024/Q3", result);
    }

    @Test
    void testNoPatternPlaceholders() {
        String result = DatePatternResolver.resolve("static/path", TEST_DATE);
        assertEquals("static/path", result);
    }

    @Test
    void testEmptyPattern() {
        String result = DatePatternResolver.resolve("", TEST_DATE);
        assertEquals("", result);
    }
}
