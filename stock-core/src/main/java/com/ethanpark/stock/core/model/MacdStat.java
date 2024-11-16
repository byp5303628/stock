package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/16
 */
@Getter
@Setter
public class MacdStat {
    /**
     * EMA移动平均值
     */
    private int start;
    private int end;
    private int deaLimit;

    private BigDecimal diff;

    private BigDecimal emaStart;

    private BigDecimal emaEnd;

    private BigDecimal dea;

    private BigDecimal macd;
}
