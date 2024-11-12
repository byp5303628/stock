package com.ethanpark.stock.core.model;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
public enum StatisticsType {

    MONTH_STAT("月度统计数据"),
    WEEK_STAT("周统计数据"),
    MACD("MACD系列指标"),
    ;

    private String description;

    StatisticsType(String description) {
        this.description = description;
    }
}
