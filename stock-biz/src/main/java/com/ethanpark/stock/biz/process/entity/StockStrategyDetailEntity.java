package com.ethanpark.stock.biz.process.entity;

import com.ethanpark.stock.biz.dto.StockRegressionDetailDTO;
import com.ethanpark.stock.biz.process.entity.aware.StockCntAware;
import com.ethanpark.stock.biz.process.entity.aware.TradePolicyAware;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Getter
@Setter
public class StockStrategyDetailEntity extends StrategyDetailEntity implements StockCntAware, TradePolicyAware {
    private String code;

    private StockRegressionDetail stockRegressionDetail;

    private StockRegressionDetailDTO resultDTO;
}
