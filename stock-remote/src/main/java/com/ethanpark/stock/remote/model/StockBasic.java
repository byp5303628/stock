package com.ethanpark.stock.remote.model;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Getter
@Setter
public class StockBasic {
    private Long id;

    private String code;

    private String name;

    private BigDecimal startPrice;

    private BigDecimal endPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    /**
     * 总手数
     */
    private BigDecimal totalValue;

    /**
     * yyyy-MM-dd
     */
    private String partitionDate;

    private Date gmtCreate;
    private Date gmtModified;
}
