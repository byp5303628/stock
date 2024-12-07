package com.ethanpark.stock.biz.process.entity;

import com.ethanpark.stock.biz.engine.entity.BaseEntity;
import com.ethanpark.stock.biz.process.entity.aware.StockCodeListAware;
import com.ethanpark.stock.biz.process.entity.aware.TradePolicyAware;
import com.ethanpark.stock.biz.trade.TradePolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class StrategyDetailRegressionEntity extends BaseEntity implements TradePolicyAware, StockCodeListAware {

    private String name;

    private TradePolicy tradePolicy;

    private List<String> stockCodes;

    @Override
    public String getTradePolicyName() {
        return name;
    }
}
