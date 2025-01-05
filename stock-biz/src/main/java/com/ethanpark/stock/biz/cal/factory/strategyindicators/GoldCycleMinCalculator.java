package com.ethanpark.stock.biz.cal.factory.strategyindicators;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.base.BaseMinCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class GoldCycleMinCalculator extends BaseMinCalculator {

    @Override
    protected Number getTargetIndicator(StockRegressionDetail stockRegressionDetail) {
        return stockRegressionDetail.getStockPredictIndicator().getGoldCycleCnt();
    }

    @Override
    public String getName() {
        return "goldCntMin";
    }

    @Override
    public String getDescription() {
        return "金叉最小值";
    }

    @Override
    public String getType() {
        return "Integer";
    }
}
