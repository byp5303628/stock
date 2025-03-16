package com.ethanpark.stock.biz.cal.factory.strategyindicators.base;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.StrategyIndicatorCalculator;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.model.indicator.Indicator;
import com.ethanpark.stock.core.model.indicator.StockPredictIndicator;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/27
 */
public abstract class BaseMidNumCalculator implements StrategyIndicatorCalculator {

    @Override
    public Indicator calculate(List<StockRegressionDetail> details) {
        Indicator indicator = buildNew();

        if (CollectionUtils.isEmpty(details)) {
            return null;
        }

        List<Number> numberList = new ArrayList<>(details.size());

        for (StockRegressionDetail detail : details) {
            StockPredictIndicator stockPredictIndicator = detail.getStockPredictIndicator();

            if (stockPredictIndicator == null) {
                continue;
            }

            Number increaseTotal = getTargetIndicator(detail);

            numberList.add(increaseTotal);
        }

        numberList.sort(Comparator.comparingDouble(Number::doubleValue));

        indicator.setValue(numberList.get(numberList.size() / 2));

        return indicator;
    }

    protected abstract Number getTargetIndicator(StockRegressionDetail stockRegressionDetail);
}
