package com.ethanpark.stock.biz.process.entity;

import com.ethanpark.stock.biz.dto.StrategyDetailDTO;
import com.ethanpark.stock.biz.engine.entity.BaseEntity;
import com.ethanpark.stock.biz.process.entity.aware.StockCntAware;
import com.ethanpark.stock.biz.process.entity.aware.StockRegressionDetailsAware;
import com.ethanpark.stock.biz.process.entity.aware.TradePolicyAware;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class StrategyDetailEntity extends BaseEntity implements StockCntAware, TradePolicyAware, StockRegressionDetailsAware {
    private String name;

    private Integer stockCnt;

    private TradePolicy tradePolicy;

    private List<StockRegressionDetail> stockRegressionDetails;

    private StrategyDetailDTO strategyDetailDTO;
    @Override
    public String getTradePolicyName() {
        return name;
    }
}
