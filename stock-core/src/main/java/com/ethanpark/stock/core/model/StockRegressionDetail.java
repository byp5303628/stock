package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@Getter
@Setter
public class StockRegressionDetail {
    private Long id;
    private String code;

    private String tradePolicyName;

    private List<TradeCycle> tradeCycles = new ArrayList<>();


}
