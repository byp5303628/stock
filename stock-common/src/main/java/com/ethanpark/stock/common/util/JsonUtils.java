package com.ethanpark.stock.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
public class JsonUtils {

    public static Map<String, String> toStringMap(String json) {
        return JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        }.getType());
    }
}
