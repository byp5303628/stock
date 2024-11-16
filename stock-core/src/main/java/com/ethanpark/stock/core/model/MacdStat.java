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

    private BigDecimal emaStart;

    private BigDecimal emaEnd;

    public BigDecimal getDiff() {
        if (emaStart == null || emaEnd == null) {
            return null;
        }

        return emaStart.subtract(emaEnd);
    }

    private BigDecimal dea;

    private BigDecimal macd;
}
