package com.ethanpark.stock.biz.process.entity.aware;

import com.ethanpark.stock.core.model.StockRegressionDetail;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
public interface StockRegressionDetailsAware extends TradePolicyAware {

    void setStockRegressionDetails(List<StockRegressionDetail> regressionDetails);
}
