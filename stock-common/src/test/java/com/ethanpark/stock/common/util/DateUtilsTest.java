package com.ethanpark.stock.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DateUtilsTest {

    @Test
    public void testFormatDate_withPattern() {
        String result = DateUtils.formatDate(new Date(1577808000000L), "yyyy-MM-dd"); // 2020-01-01
        Assertions.assertEquals("2020-01-01", result);
    }

    @Test
    public void testFormatDate_null_returnsNull() {
        String result = DateUtils.formatDate(null);
        Assertions.assertNull(result);
    }

    @Test
    public void testFormatDate_defaultPattern() {
        String result = DateUtils.formatDate(new Date(1577808000000L));
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("2020-01-01"));
    }

    @Test
    public void testGetToday() {
        String today = DateUtils.getToday();
        Assertions.assertNotNull(today);
        Assertions.assertTrue(today.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testParseStringToDate() {
        java.time.LocalDateTime result = DateUtils.parseStringToDate("2020-01-01", "yyyy-MM-dd");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2020, result.getYear());
        Assertions.assertEquals(1, result.getMonthValue());
        Assertions.assertEquals(1, result.getDayOfMonth());
    }

    @Test
    public void testPlusDay_defaultCount() {
        String result = DateUtils.plusDay("2020-01-01");
        Assertions.assertEquals("2020-01-02", result);
    }

    @Test
    public void testPlusDay_customCount() {
        String result = DateUtils.plusDay("2020-01-01", 5);
        Assertions.assertEquals("2020-01-06", result);
    }

    @Test
    public void testPlusDay_crossMonth() {
        String result = DateUtils.plusDay("2020-01-30", 5);
        Assertions.assertEquals("2020-02-04", result);
    }

    @Test
    public void testPlusDay_invalidInput_returnsNull() {
        String result = DateUtils.plusDay("invalid-date", 1);
        Assertions.assertNull(result);
    }

    @Test
    public void testPlusYear() {
        String resultDate = DateUtils.plusYear("2019-01-01");
        Assertions.assertEquals("2020-01-01", resultDate);
    }

    @Test
    public void testPlusYear_invalidInput_returnsNull() {
        String result = DateUtils.plusYear("not-a-date");
        Assertions.assertNull(result);
    }

    @Test
    public void testGetWeekOfYear() {
        int weekOfYear = DateUtils.getWeekOfYear("2019-01-01");
        Assertions.assertEquals(1, weekOfYear);
    }

    @Test
    public void testMonthDiff_normal() {
        double diff = DateUtils.monthDiff("2020-01-01", "2020-06-01");
        Assertions.assertEquals(5.0, diff, 0.1);
    }

    @Test
    public void testMonthDiff_reverseOrder_returnsPositive() {
        double diff = DateUtils.monthDiff("2020-06-01", "2020-01-01");
        Assertions.assertEquals(5.0, diff, 0.1);
    }

    @Test
    public void testMonthDiff_sameDate_returnsZero() {
        double diff = DateUtils.monthDiff("2020-01-01", "2020-01-01");
        Assertions.assertEquals(0.0, diff, 0.01);
    }

    @Test
    public void testMonthDiff_nullInput_returnsZero() {
        double diff = DateUtils.monthDiff(null, "2020-01-01");
        Assertions.assertEquals(0.0, diff, 0.01);
    }

    @Test
    public void testMonthDiff_emptyInput_returnsZero() {
        double diff = DateUtils.monthDiff("", "2020-01-01");
        Assertions.assertEquals(0.0, diff, 0.01);
    }

    @Test
    public void testYearDiff_normal() {
        double diff = DateUtils.yearDiff("2020-01-01", "2021-01-01");
        Assertions.assertEquals(1.0, diff, 0.1);
    }

    @Test
    public void testYearDiff_nullInput_returnsZero() {
        double diff = DateUtils.yearDiff(null, "2020-01-01");
        Assertions.assertEquals(0.0, diff, 0.01);
    }

    @Test
    public void testDayDiff_normal() {
        int diff = DateUtils.dayDiff("2020-01-01", "2020-01-10");
        Assertions.assertEquals(9, diff);
    }

    @Test
    public void testDayDiff_negative_becomesPositive() {
        int diff = DateUtils.dayDiff("2020-01-10", "2020-01-01");
        Assertions.assertEquals(9, diff);
    }

    @Test
    public void testGetMonthOfYear() {
        int month = DateUtils.getMonthOfYear("2020-03-15");
        Assertions.assertEquals(3, month);
    }

    @Test
    public void testGetMonthOfYear_january() {
        int month = DateUtils.getMonthOfYear("2020-01-01");
        Assertions.assertEquals(1, month);
    }
}