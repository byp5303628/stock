package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/17
 */
@Getter
@Setter
public class StockRegressionDetailDTO {
    private String code;

    private StrategyDetailDTO strategyDetailDTO;

    private StockPredictIndicatorDTO stockPredictIndicator;

    private List<TradeCycleDTO> tradeCycles;
}
