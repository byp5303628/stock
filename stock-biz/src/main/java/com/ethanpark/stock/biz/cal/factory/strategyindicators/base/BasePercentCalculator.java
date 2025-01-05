package com.ethanpark.stock.biz.cal.factory.strategyindicators.base;

import com.ethanpark.stock.biz.cal.factory.StrategyIndicatorCalculator;
import com.ethanpark.stock.biz.cal.factory.strategyindicators.IndicatorType;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.model.indicator.Indicator;
import com.ethanpark.stock.core.model.indicator.StockPredictIndicator;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
public abstract class BasePercentCalculator implements StrategyIndicatorCalculator {
    @Override
    public Indicator calculate(List<StockRegressionDetail> details) {
        Indicator indicator = buildNew();

        if (CollectionUtils.isEmpty(details)) {
            return null;
        }

        int matchCnt = 0;
        int cnt = 0;

        for (StockRegressionDetail detail : details) {
            StockPredictIndicator stockPredictIndicator = detail.getStockPredictIndicator();

            if (stockPredictIndicator == null) {
                continue;
            }

            cnt++;

            if (match(detail)) {
                matchCnt++;
            }
        }

        if (cnt == 0) {
            return null;
        }

        indicator.setValue(matchCnt * 1D / cnt);

        return indicator;
    }

    protected abstract boolean match(StockRegressionDetail detail);

    @Override
    public String getType() {
        return IndicatorType.PERCENT;
    }
}
