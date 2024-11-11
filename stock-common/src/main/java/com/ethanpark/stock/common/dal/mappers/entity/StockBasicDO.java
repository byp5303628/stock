package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

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

    private BigDecimal totalValue;

    /**
     * yyyy-MM-dd
     */
    private String partitionDate;

    private Date gmtCreate;

    private Date gmtModified;
}
