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
 * macd > 0, diff > 0, dea > 0 macd 比前一天数据大, 进行买入 否则卖出
 *
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Service
public class MacdDailyV1TradePolicy extends BaseTradePolicy {

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
            BigDecimal diff = statistic.getStatistic("diff");
            BigDecimal dea = statistic.getStatistic("dea");
            BigDecimal macd = statistic.getStatistic("macd");

            if (diff.compareTo(BigDecimal.ZERO) <= 0) {
                tradeContext.sale(stockBasic);
                continue;
            }

            if (dea.compareTo(BigDecimal.ZERO) <= 0) {
                tradeContext.sale(stockBasic);
                continue;
            }

            if (macd.compareTo(BigDecimal.ZERO) <= 0) {
                tradeContext.sale(stockBasic);
                continue;
            }

            // macd 今天比昨天低
            if (i != 0 && macd.compareTo(statistics.get(i - 1).getStatistic("macd")) <= 0) {
                tradeContext.sale(stockBasic);
                continue;
            }

            tradeContext.purchase(stockBasic);
        }

        return tradeContext;
    }

    @Override
    public String getDescription() {
        return "macd > 0, diff > 0, dea > 0 macd 比前一天数据大, 进行买入 否则卖出";
    }

    @Override
    public List<StatisticsType> getStatisticsTypes() {
        return Arrays.asList(StatisticsType.MACD);
    }
}
