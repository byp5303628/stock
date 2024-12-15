package com.ethanpark.stock.biz.process.entity;

import com.ethanpark.stock.biz.engine.entity.BaseEntity;
import com.ethanpark.stock.biz.process.entity.aware.StockCntAware;
import com.ethanpark.stock.biz.process.entity.aware.TradePolicyAware;
import com.ethanpark.stock.biz.trade.TradePolicy;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Getter
@Setter
public class StockStrategyDetailEntity extends BaseEntity implements StockCntAware, TradePolicyAware {
    private String code;
    private String name;

    private Integer stockCnt;

    private TradePolicy tradePolicy;

    @Override
    public String getTradePolicyName() {
        return name;
    }
}
