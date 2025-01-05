package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseAvgCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class MonthIncreaseAvgCalculator extends BaseAvgCalculator {

    @Override
    protected Number getTargetIndicator(StockRegressionDetail stockRegressionDetail) {
        return stockRegressionDetail.getStockPredictIndicator().getMonthIncrease();
    }

    @Override
    public String getName() {
        return "MonthIncreaseAvg";
    }

    @Override
    public String getDescription() {
        return "月度增长平均值";
    }

    @Override
    public String getType() {
        return IndicatorType.PERCENT;
    }
}
