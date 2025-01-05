package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseCntCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class CalculatedCntCalculator extends BaseCntCalculator {
    @Override
    protected boolean match(StockRegressionDetail detail) {
        return detail.getStockPredictIndicator().getGoldCycleCnt() > 0;
    }

    @Override
    public String getName() {
        return "CalculatedCnt";
    }

    @Override
    public String getDescription() {
        return "参与统计的股票个数";
    }
}
