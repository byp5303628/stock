package com.ethanpark.stock.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

public class JsonUtilsTest {

    @Test
    public void testToStringMap_normal() {
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        Map<String, String> result = JsonUtils.toStringMap(json);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("value1", result.get("key1"));
        Assertions.assertEquals("value2", result.get("key2"));
    }

    @Test
    public void testToStringMap_empty() {
        String json = "{}";

        Map<String, String> result = JsonUtils.toStringMap(json);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testToBigDecimalMap_normal() {
        String json = "{\"price\":\"10.55\",\"amount\":\"99.99\"}";

        Map<String, BigDecimal> result = JsonUtils.toBigDecimalMap(json);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(new BigDecimal("10.55"), result.get("price"));
        Assertions.assertEquals(new BigDecimal("99.99"), result.get("amount"));
    }

    @Test
    public void testToBigDecimalMap_empty() {
        String json = "{}";

        Map<String, BigDecimal> result = JsonUtils.toBigDecimalMap(json);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}
