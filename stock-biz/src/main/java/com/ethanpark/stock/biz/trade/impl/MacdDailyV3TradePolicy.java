package com.ethanpark.stock.biz.trade.impl;

import com.ethanpark.stock.core.model.StockContext;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.core.model.TradeContext;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * macd > 0, diff > 0, dea > 0, 金叉买, 死叉卖
 *
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Service
public class MacdDailyV3TradePolicy extends BaseTradePolicy {

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

            tradeContext.purchase(stockBasic);
        }

        return tradeContext;
    }

    @Override
    public String getDescription() {
        return "macd > 0, diff > 0, dea > 0 金叉买入, 死叉卖出";
    }
}
