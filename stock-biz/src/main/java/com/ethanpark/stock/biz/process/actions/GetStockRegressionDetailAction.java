package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.process.entity.StockStrategyDetailEntity;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.service.StockRegressionDomainService;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Action("getStockRegressionDetailAction")
public class GetStockRegressionDetailAction implements BusinessAction {

    @Resource
    private StockRegressionDomainService stockRegressionDomainService;

    @Override
    public void process(ProcessContext context) {
        StockStrategyDetailEntity entity = context.getEntity();

        String code = entity.getCode();
        String name = entity.getName();

        StockRegressionDetail regressionDetailByCodeAndPolicy = stockRegressionDomainService.getRegressionDetailByCodeAndPolicy(code, name);

        if (regressionDetailByCodeAndPolicy == null) {
            return;
        }

        entity.setStockRegressionDetail(regressionDetailByCodeAndPolicy);
    }
}
