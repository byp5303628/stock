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
public abstract class BaseAvgCalculator implements StrategyIndicatorCalculator {

    @Override
    public Indicator calculate(List<StockRegressionDetail> details) {
        Indicator indicator = buildNew();

        if (CollectionUtils.isEmpty(details)) {
            return null;
        }

        int cnt = 0;
        Double sum = 0D;

        for (StockRegressionDetail detail : details) {
            StockPredictIndicator stockPredictIndicator = detail.getStockPredictIndicator();

            if (stockPredictIndicator == null) {
                continue;
            }

            Number increaseTotal = getTargetIndicator(detail);
            cnt += 1;

            sum += increaseTotal.doubleValue();
        }

        if (cnt == 0) {
            return null;
        }

        indicator.setValue(sum / cnt);

        return indicator;
    }

    protected abstract Number getTargetIndicator(StockRegressionDetail stockRegressionDetail);

    @Override
    public String getType() {
        return IndicatorType.DOUBLE;
    }
}
