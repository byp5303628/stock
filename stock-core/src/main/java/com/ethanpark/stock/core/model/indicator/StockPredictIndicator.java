package com.ethanpark.stock.core.model.indicator;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Getter
@Setter
public class StockPredictIndicator {
    private String code;

    private String name;

    /**
     * 金叉个数
     */
    private int goldCycleCnt = 0;

    /**
     * 策略起始日期
     */
    private String startDate;

    /**
     * 策略结束日期
     */
    private String endDate;

    /**
     * 总盈利算数总值
     */
    private Double increaseTotal = 0D;

    /**
     * 总盈利实际总值
     */
    private Double increaseActualTotal = 0D;

    /**
     * 盈利算数平均
     */
    private Double increaseAvg = 0D;

    /**
     * 盈利实际平均
     */
    private Double increaseActualAvg = 0D;

    /**
     * 单周期最大增长
     */
    private Double increaseMax = 0D;

    /**
     * 单周期最小增长
     */
    private Double increaseMin = 0D;

    /**
     * 月均增长
     */
    private Double monthIncrease = 0D;

    /**
     * 年均增长
     */
    private Double yearIncrease = 0D;

    /**
     * 盈利分布
     */
    private List<Histogram> histograms = new ArrayList<>();
}
