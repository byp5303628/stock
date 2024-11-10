package com.ethanpark.stock.common.util;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

    @Test
    public void testPlusYear() {
        String date = "2019-01-01";

        String resultDate = DateUtils.plusYear(date);

        Assert.assertEquals(resultDate, "2020-01-01");
    }
}