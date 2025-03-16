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
        List<StockStatistics> statistics = context.getMacdStatsMap().get("MACD_12_26");
        List<StockBasic> hfqStocks = context.getHfqStocks();

        for (int i = 0; i < statistics.size(); i++) {
            StockBasic stockBasic = hfqStocks.get(i);

            if (stockBasic.getPartitionDate().compareTo("2014-01-01") < 0) {
                continue;
            }

            StockStatistics statistic = statistics.get(i);
            BigDecimal macd = statistic.getStatistic("macd");


            if (macd.compareTo(BigDecimal.ZERO) <= 0) {
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
