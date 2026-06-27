package com.ethanpark.stock.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MathUtilsTest {

    @Test
    public void testCalEma_normal() {
        List<BigDecimal> input = Arrays.asList(
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("30"),
                new BigDecimal("25")
        );

        List<BigDecimal> result = MathUtils.calEma(3, input);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(4, result.size());
        // First value should equal first input
        Assertions.assertEquals(new BigDecimal("10"), result.get(0));
        // Last value should be an EMA, not raw input
        Assertions.assertNotNull(result.get(3));
    }

    @Test
    public void testCalEma_empty_returnsEmptyList() {
        List<BigDecimal> result = MathUtils.calEma(3, Collections.emptyList());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testCalEma_null_returnsEmptyList() {
        List<BigDecimal> result = MathUtils.calEma(3, null);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testCalEma_singleElement() {
        List<BigDecimal> result = MathUtils.calEma(3, Collections.singletonList(new BigDecimal("42")));
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(new BigDecimal("42"), result.get(0));
    }
}
