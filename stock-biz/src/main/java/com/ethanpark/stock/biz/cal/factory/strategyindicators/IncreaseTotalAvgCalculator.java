package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseAvgCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class IncreaseTotalAvgCalculator extends BaseAvgCalculator {

    @Override
    public String getName() {
        return "IncreaseTotalAvg";
    }

    @Override
    public String getDescription() {
        return "增长平均值";
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
