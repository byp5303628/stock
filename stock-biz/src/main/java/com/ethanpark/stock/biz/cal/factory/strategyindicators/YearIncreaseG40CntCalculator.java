package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseCntCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class YearIncreaseG40CntCalculator extends BaseCntCalculator {
    @Override
    protected boolean match(StockRegressionDetail detail) {
        return detail.getStockPredictIndicator().getYearIncrease() > 0.4D;
    }

    @Override
    public String getDescription() {
        return "年增长大于40%的股票数";
    }
}
