package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import com.ethanpark.stock.biz.process.entity.aware.TradePolicyAware;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.biz.trade.TradePolicyFactory;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Action("getTradePolicyByNameAction")
public class GetTradePolicyByNameAction implements BusinessAction {

    @Resource
    private TradePolicyFactory tradePolicyFactory;

    @Override
    public void process(ProcessContext context) {
        TradePolicyAware entity = context.getEntity();

        String name = entity.getTradePolicyName();

        TradePolicy tradePolicy = tradePolicyFactory.getTradePolicy(name);

        if (tradePolicy == null) {
            throw new ProcessException(ErrorCode.ILLEGAL_PARAM.getCode(), "对应股票策略不存在");
        }

        entity.setTradePolicy(tradePolicy);
    }
}
