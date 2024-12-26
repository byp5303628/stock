package com.ethanpark.stock.biz.dto;

import com.ethanpark.stock.remote.model.StockBasic;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/27
 */
@Getter
@Setter
public class TradeCycleDTO {
    private String purchaseDate;

    private String saleDate;

    private Double increase;

    private String purchasePrice;

    private String salePrice;

    private StockBasic purchaseDetail;

    private StockBasic saleDetail;

    private Integer goldDays;


}
