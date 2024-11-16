package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Getter
@Setter
public class StockStatisticsDO {
    private Long id;

    private String code;

    private String partitionDate;

    /**
     * 指标类型, 用于区分不同的指标系列
     */
    private String statisticsType;

    /**
     * 指标名称, 指标类型一致的时候, 用于区分子指标
     */
    private String statisticsName;

    /**
     * Key为指标的名称
     */
    private String statistics;
}
