package com.ethanpark.stock.biz.trade;

import com.ethanpark.stock.core.model.TradeContext;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
public interface TradePolicy {

    TradeContext trade(String code);

    /**
     * 描述一个策略大概怎么执行的
     *
     * @return
     */
    String getDescription();
}
