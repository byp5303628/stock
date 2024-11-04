package com.ethanpark.stock.common.model;


import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Getter
@Setter
public class StockBasic {
    private String code;

    private String name;

    private Double startPrice;

    private Double endPrice;

    private Double highestPrice;

    private Double lowestPrice;

    /**
     * yyyyMMdd
     */
    private String partitionDate;
}
