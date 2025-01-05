package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseAvgCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class YearIncreaseAvgCalculator extends BaseAvgCalculator {

    @Override
    protected Number getTargetIndicator(StockRegressionDetail stockRegressionDetail) {
        return stockRegressionDetail.getStockPredictIndicator().getYearIncrease();
    }

    @Override
    public String getName() {
        return "YearIncreaseAvg";
    }

    @Override
    public String getDescription() {
        return "年度增长平均值";
    }
}
