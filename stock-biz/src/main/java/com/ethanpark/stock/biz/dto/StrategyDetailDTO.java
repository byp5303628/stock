package com.ethanpark.stock.biz.dto;

import com.ethanpark.stock.core.model.indicator.Indicator;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class StrategyDetailDTO {
    private String name;

    private List<String> tags;

    private String description;

    private Integer verifyStockCnt = 0;

    private Integer totalStockCnt = 0;

    private List<Indicator> indicators;

    private List<StockPredictIndicatorDTO> stockPredictIndicators;
}
