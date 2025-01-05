package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BasePercentCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class CalculatedPercentCalculator extends BasePercentCalculator {
    @Override
    protected boolean match(StockRegressionDetail detail) {
        return detail.getStockPredictIndicator().getGoldCycleCnt() > 0;
    }

    @Override
    public String getName() {
        return "CalculatedPercent";
    }

    @Override
    public String getDescription() {
        return "参与统计的股票占比";
    }
}
