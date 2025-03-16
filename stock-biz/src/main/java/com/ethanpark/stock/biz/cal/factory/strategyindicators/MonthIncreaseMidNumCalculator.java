package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseMidNumCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/27
 */
@Service
public class MonthIncreaseMidNumCalculator extends BaseMidNumCalculator {
    @Override
    protected Number getTargetIndicator(StockRegressionDetail stockRegressionDetail) {
        return stockRegressionDetail.getStockPredictIndicator().getMonthIncrease();
    }

    @Override
    public String getDescription() {
        return "月度增长中位数";
    }


    @Override
    public String getType() {
        return IndicatorType.PERCENT;
    }
}
