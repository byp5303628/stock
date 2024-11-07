package com.ethanpark.stock.remote.model;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Getter
@Setter
public class StockBasic {
    private String code;

    private String name;

    private BigDecimal startPrice;

    private BigDecimal endPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    /**
     * yyyy-MM-dd
     */
    private String partitionDate;
}
