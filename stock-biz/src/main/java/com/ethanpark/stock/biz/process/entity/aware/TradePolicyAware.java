package com.ethanpark.stock.biz.process.entity.aware;

import com.ethanpark.stock.biz.trade.TradePolicy;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public interface TradePolicyAware {
    String getTradePolicyName();

    void setTradePolicy(TradePolicy tradePolicy);
}
