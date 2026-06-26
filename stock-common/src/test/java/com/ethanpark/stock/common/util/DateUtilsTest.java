package com.ethanpark.stock.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    @Test
    public void testPlusYear() {
        String date = "2019-01-01";

        String resultDate = DateUtils.plusYear(date);

        Assertions.assertEquals(resultDate, "2020-01-01");
    }

    @Test
    public void testGetWeekOfYear() {
        String date = "2019-01-01";

        int weekOfYear = DateUtils.getWeekOfYear(date);

        Assertions.assertEquals(weekOfYear, 1);
    }
}
