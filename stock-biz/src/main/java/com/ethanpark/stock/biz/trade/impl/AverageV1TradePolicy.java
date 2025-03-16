package com.ethanpark.stock.biz.trade.impl;

import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockContext;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.core.model.TradeContext;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 均线交易策略。
 *
 * @author: baiyunpeng04
 * @since: 2025/3/15
 */
@Service
public class AverageV1TradePolicy extends BaseTradePolicy {
    @Override
    public TradeContext trade0(StockContext context, TradeContext tradeContext) {

        Map<String, StockStatistics> averageStatsMap = context.getAverageStatsMap();

        List<StockBasic> hfqStocks = context.getHfqStocks();

        for (int i = 1; i < hfqStocks.size(); i++) {
            StockBasic stockBasic = hfqStocks.get(i);

            StockBasic lastStockBasic = hfqStocks.get(i - 1);

            String partitionDate = lastStockBasic.getPartitionDate();
            if (partitionDate.compareTo("2014-01-01") < 0) {
                continue;
            }

            StockStatistics lastStockStatistics = averageStatsMap.get(partitionDate);
            StockStatistics stockStatistics = averageStatsMap.get(stockBasic.getPartitionDate());

            if (lastStockStatistics == null || stockStatistics == null) {
                continue;
            }

            BigDecimal dayAvg5 = stockStatistics.getStatistic("dayAvg5");
            BigDecimal dayAvg25 = stockStatistics.getStatistic("dayAvg25");
            BigDecimal dayAvg150 = stockStatistics.getStatistic("dayAvg150");

            BigDecimal lastDayAvg5 = lastStockStatistics.getStatistic("dayAvg5");
            BigDecimal lastDayAvg25 = lastStockStatistics.getStatistic("dayAvg25");
            BigDecimal lastDayAvg150 = lastStockStatistics.getStatistic("dayAvg150");

            if (dayAvg150.compareTo(lastDayAvg150) <= 0
                    || dayAvg25.compareTo(lastDayAvg150) <= 0
                    || dayAvg5.compareTo(lastDayAvg5) <= 0
            ) {
                tradeContext.sale(stockBasic);
            } else {
                tradeContext.purchase(stockBasic);
            }
        }

        return tradeContext;
    }

    @Override
    public String getDescription() {
        return "均线交易策略， 25日均线上窜 150日均线；25日均线高于昨天 25日均线，150均线高于昨天 150日均线，5日均线高于昨日 5 日均线，进行买入，否则卖出";
    }

    @Override
    public List<StatisticsType> getStatisticsTypes() {
        return Arrays.asList(StatisticsType.DAY_STAT);
    }
}
