package com.ethanpark.stock.biz.dto;

import com.ethanpark.stock.core.model.TradeCycle;
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

    private StockPredictIndicatorDTO stockPredictIndicator;

    private List<TradeCycle> tradeCycles;
}
