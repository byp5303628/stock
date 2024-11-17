package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
@Getter
@Setter
public class StockStatistics {
    private Long id;

    private String code;

    private String partitionDate;

    /**
     * 指标类型, 用于区分不同的指标系列
     */
    private StatisticsType statisticsType;

    /**
     * 指标名称, 指标类型一致的时候, 用于区分子指标
     */
    private String statisticsName;

    /**
     * Key为指标的名称
     */
    private Map<String, BigDecimal> statistics = new HashMap<>();

    public BigDecimal getStatistic(String key) {
        return statistics.get(key);
    }
}
