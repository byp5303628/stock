package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.util.DateUtils;
import com.ethanpark.stock.core.model.TradeCycle;
import com.ethanpark.stock.core.model.indicator.Histogram;
import com.ethanpark.stock.core.model.indicator.StockPredictIndicator;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        TradeCycle last = first;


        Double increaseTotal = 1D;
        Double increaseActual = 1D;

        Double increaseMax = -1000D;
        Double increaseMin = 1000D;

        for (TradeCycle tradeCycle : tradeCycles) {
            if (!tradeCycle.isValid()) {
                continue;
            }

            increaseTotal = (tradeCycle.getIncrease() + 1) * increaseTotal;
            increaseActual = tradeCycle.getIncrease() + increaseActual;

            if (tradeCycle.getIncrease() > increaseMax) {
                increaseMax = tradeCycle.getIncrease();
            }

            if (tradeCycle.getIncrease() < increaseMin) {
                increaseMin = tradeCycle.getIncrease();
            }
            last = tradeCycle;
        }

        indicator.setStartDate(first.getStartDate());
        indicator.setEndDate(last.getEndDate());
        indicator.setIncreaseTotal(increaseTotal);
        indicator.setIncreaseActualTotal(increaseActual);
        indicator.setIncreaseMax(increaseMax);
        indicator.setIncreaseMin(increaseMin);
        indicator.setIncreaseAvg(increaseTotal / tradeCycles.size());
        indicator.setIncreaseActualAvg(increaseActual / tradeCycles.size());

        indicator.setMonthIncrease(increaseActual / DateUtils.monthDiff(indicator.getStartDate(),
                indicator.getEndDate()));
        indicator.setYearIncrease(increaseActual / DateUtils.yearDiff(indicator.getStartDate(),
                indicator.getEndDate()));

        List<Histogram> hists = tradeCycles
                .stream()
                .map(i -> {
                    Histogram histogram = new Histogram();

                    histogram.setName(String.format("%s-%s", i.getStartDate(), i.getEndDate()));
                    histogram.setValue(i.getIncrease());
                    return histogram;
                })
                .filter(i -> i.getValue() != null)
                .sorted(Comparator.comparingDouble(Histogram::getValue))
                .collect(Collectors.toList());

        indicator.setHistograms(hists);

        return indicator;
    }
}
