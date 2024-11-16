package com.ethanpark.stock.biz.cal.impl;

import com.ethanpark.stock.biz.cal.StatisticsStrategy;
import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.remote.model.StockBasic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
public class MonthStatStatisticsStrategy implements StatisticsStrategy {

    /**
     * 按照月维度进行group by 然后计算其中的对应的四个价格以及累计成交量
     *
     * @param stockBasicList
     * @return
     */
    @Override
    public List<StockStatistics> calculate(List<StockBasic> stockBasicList) {

        Map<String, List<StockBasic>> map =
                stockBasicList.stream()
                        .collect(Collectors.groupingBy(i -> (i.getPartitionDate().substring(0, 7))));

        List<StockStatistics> statistics = new ArrayList<>();

        for (Map.Entry<String, List<StockBasic>> entry : map.entrySet()) {
            List<StockBasic> values = entry.getValue();

            values.sort(new Comparator<StockBasic>() {
                @Override
                public int compare(StockBasic o1, StockBasic o2) {
                    return o1.getPartitionDate().compareTo(o2.getPartitionDate());
                }
            });

            StockStatistics statistic = new StockStatistics();
            statistic.setCode(values.get(0).getCode());
            statistic.setPartitionDate(values.get(0).getPartitionDate().substring(0, 7) + "-01");
            statistic.setStatisticType(getStatisticsType());

            BigDecimal startPrice = values.get(0).getStartPrice();
            BigDecimal endPrice = values.get(values.size() - 1).getEndPrice();
            BigDecimal lowestPrice = new BigDecimal(100000000);
            BigDecimal highestPrice = new BigDecimal(0);
            BigDecimal tv = new BigDecimal(0);

            for (StockBasic value : values) {
                if (highestPrice.compareTo(value.getHighestPrice()) < 0) {
                    highestPrice = value.getHighestPrice();
                }

                if (lowestPrice.compareTo(value.getLowestPrice()) > 0) {
                    lowestPrice = value.getLowestPrice();
                }

                tv = tv.add(value.getTotalValue());
            }

            statistic.getStatistics().put("startPrice", startPrice);
            statistic.getStatistics().put("endPrice", endPrice);
            statistic.getStatistics().put("lowestPrice", lowestPrice);
            statistic.getStatistics().put("tv", tv);
            statistic.getStatistics().put("highestPrice", highestPrice);

            statistics.add(statistic);
        }

        statistics.sort(new Comparator<StockStatistics>() {
            @Override
            public int compare(StockStatistics o1, StockStatistics o2) {
                return o1.getPartitionDate().compareTo(o2.getPartitionDate());
            }
        });
        return statistics;
    }

    @Override
    public StatisticsType getStatisticsType() {
        return StatisticsType.MACD;
    }
}
