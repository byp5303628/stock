package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseCntCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class YearIncreaseG0CntCalculator extends BaseCntCalculator {
    @Override
    protected boolean match(StockRegressionDetail detail) {
        return detail.getStockPredictIndicator().getYearIncrease() > 0.05D;
    }

    @Override
    public String getDescription() {
        return "年增长大于0的股票数";
    }
}
