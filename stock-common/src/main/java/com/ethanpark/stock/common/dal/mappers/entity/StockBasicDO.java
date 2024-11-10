package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Data
public class StockBasicDO {
    private Long id;

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
