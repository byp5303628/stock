package com.ethanpark.stock.core.model;

import com.alibaba.druid.util.StringUtils;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
public enum StatisticsType {

    MONTH_STAT("月度统计数据"),
    WEEK_STAT("周统计数据"),
    DAY_STAT("日统计数据"),
    MACD("MACD系列指标"),
    ;

    private String description;

    StatisticsType(String description) {
        this.description = description;
    }

    public static StatisticsType getByName(String name) {
        for (StatisticsType value : StatisticsType.values()) {
            if (StringUtils.equals(name, value.name())) {
                return value;
            }
        }

        return null;
    }
}
