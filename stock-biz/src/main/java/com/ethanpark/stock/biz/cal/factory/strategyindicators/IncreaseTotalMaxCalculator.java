package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseMaxCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class IncreaseTotalMaxCalculator extends BaseMaxCalculator {

    @Override
    protected Number getTargetIndicator(StockRegressionDetail stockRegressionDetail) {
        return stockRegressionDetail.getStockPredictIndicator().getIncreaseActualTotal();
    }

    @Override
    public String getName() {
        return "IncreaseTotalMax";
    }

    @Override
    public String getDescription() {
        return "增长最大值";
    }

    @Override
    public String getType() {
        return IndicatorType.PERCENT;
    }
}
