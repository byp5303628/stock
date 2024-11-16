package com.ethanpark.stock.common.util;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
public class MathUtils {
    public static List<BigDecimal> calEma(int limit, List<BigDecimal> decimals) {
        if (CollectionUtils.isEmpty(decimals)) {
            return Collections.emptyList();
        }

        List<BigDecimal> results = new ArrayList<>(decimals.size());

        results.add(decimals.get(0));

        for (int i = 1; i < decimals.size(); i++) {
            BigDecimal bigDecimal = decimals.get(i);

            BigDecimal result =
                    new BigDecimal(bigDecimal.doubleValue() * 2D / (limit + 1) + results.get(i - 1).doubleValue() * (limit - 1) / (limit + 1));

            results.add(result);
        }

        return results;
    }
}
