package com.ethanpark.stock.biz.trade.impl;

import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockContext;
import com.ethanpark.stock.core.model.TradeContext;
import com.ethanpark.stock.core.service.StockBasicDomainService;
import com.ethanpark.stock.core.service.StockStatisticsDomainService;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
public abstract class BaseTradePolicy implements TradePolicy {

    @Resource
    private StockStatisticsDomainService stockStatisticsDomainService;

    @Resource
    private StockBasicDomainService stockBasicDomainService;

    @Override
    public TradeContext trade(String code) {
        StockContext context = new StockContext();

        context.setHfqStocks(stockBasicDomainService.queryAllHfqStockBasics(code));

        context.setMacdStatsMap(stockStatisticsDomainService.queryStats(code, StatisticsType.MACD));
        context.setMonthStats(stockStatisticsDomainService.queryStats(code,
                StatisticsType.MONTH_STAT).getOrDefault(StatisticsType.MONTH_STAT.name(),
                Collections.emptyList()));


        TradeContext tradeContext = new TradeContext();
        tradeContext.setCode(code);
        tradeContext.setStrategyDesc(getDescription());
        return trade0(context, tradeContext);
    }

    public abstract TradeContext trade0(StockContext stockContext, TradeContext tradeContext);
}
