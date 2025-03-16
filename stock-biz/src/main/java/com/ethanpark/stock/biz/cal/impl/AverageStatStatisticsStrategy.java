package com.ethanpark.stock.biz.cal.impl;

import com.ethanpark.stock.biz.cal.StatisticsStrategy;
import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
public class AverageStatStatisticsStrategy implements StatisticsStrategy {

    /**
     * 按照5日，10日，25日，150日，计算其中对应的 4 个价格
     *
     * @param stockBasicList
     * @return
     */
    @Override
    public List<StockStatistics> calculate(List<StockBasic> stockBasicList) {
        List<BigDecimal> endPriceList =
                stockBasicList.stream().map(i -> i.getEndPrice()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(endPriceList)) {
            return Collections.emptyList();
        }

        List<StockStatistics> statistics = new ArrayList<>();

        for (int i = 150; i < stockBasicList.size(); i++) {
            StockBasic stockBasic = stockBasicList.get(i);

            StockStatistics avg = new StockStatistics();
            avg.setCode(stockBasic.getCode());
            avg.setStatisticsType(getStatisticsType());
            avg.setPartitionDate(stockBasic.getPartitionDate());
            avg.setStatisticsName(getName());

            buildStockStatistics(avg, stockBasicList, i, 5);
            buildStockStatistics(avg, stockBasicList, i, 10);
            buildStockStatistics(avg, stockBasicList, i, 25);
            buildStockStatistics(avg, stockBasicList, i, 150);

            statistics.add(avg);
        }

        return statistics;
    }

    private void buildStockStatistics(StockStatistics avg, List<StockBasic> stockBasicList, int index,
                                                 int num) {
        BigDecimal sum = new BigDecimal(0);
        for (int j = 0; j < num; j++) {
            sum.add(stockBasicList.get(index - j).getEndPrice());
        }

        if (CollectionUtils.isEmpty(avg.getStatistics())) {
            avg.setStatistics(new HashMap<>());
        }

        avg.getStatistics().put(String.format("dayAvg%d", num), sum.divide(new BigDecimal(num)));
    }

    @Override
    public StatisticsType getStatisticsType() {
        return StatisticsType.DAY_STAT;
    }
}
