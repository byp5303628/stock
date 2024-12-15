package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.TradeCycle;
import com.ethanpark.stock.core.model.indicator.StockPredictIndicator;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Service
public class IndicatorDomainService {

    public StockPredictIndicator calculate(List<TradeCycle> tradeCycles) {
        if (CollectionUtils.isEmpty(tradeCycles)) {
            return null;
        }

        StockPredictIndicator indicator = new StockPredictIndicator();

        indicator.setGoldCycleCnt(tradeCycles.size());
        TradeCycle first = tradeCycles.get(0);

        TradeCycle last = tradeCycles.get(tradeCycles.size() - 1);

        indicator.setStartDate(first.getStartDate());
        indicator.setEndDate(last.getEndDate());

        Double increaseTotal = 1D;
        Double increaseActual = 1D;

        Double increaseMax = -1000D;
        Double increaseMin = 1000D;

        for (TradeCycle tradeCycle : tradeCycles) {
            increaseTotal = (tradeCycle.getIncrease() + 1) * increaseTotal;
            increaseActual = tradeCycle.getIncrease() + increaseActual;

            if (tradeCycle.getIncrease() > increaseMax) {
                increaseMax = tradeCycle.getIncrease();
            }

            if (tradeCycle.getIncrease() < increaseMin) {
                increaseMin = tradeCycle.getIncrease();
            }
        }

        indicator.setIncreaseTotal(increaseTotal);
        indicator.setIncreaseActualTotal(increaseActual);
        indicator.setIncreaseMax(increaseMax);
        indicator.setIncreaseMin(increaseMin);
        indicator.setIncreaseAvg(increaseTotal / tradeCycles.size());
        indicator.setIncreaseActualAvg(increaseActual / tradeCycles.size());

        Double range = increaseMax - increaseMin / 18;

        Map<String, Integer> map = new HashMap<>();

        return indicator;
    }
}
