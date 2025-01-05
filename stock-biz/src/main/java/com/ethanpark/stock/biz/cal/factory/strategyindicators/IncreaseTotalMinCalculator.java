package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseMinCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class IncreaseTotalMinCalculator extends BaseMinCalculator {

    @Override
    public String getName() {
        return "IncreaseTotalMin";
    }

    @Override
    public String getDescription() {
        return "增长最小值";
    }

    @Override
    protected Number getTargetIndicator(StockRegressionDetail stockRegressionDetail) {
        return stockRegressionDetail.getStockPredictIndicator().getIncreaseActualTotal();
    }

    @Override
    public String getType() {
        return IndicatorType.PERCENT;
    }
}
