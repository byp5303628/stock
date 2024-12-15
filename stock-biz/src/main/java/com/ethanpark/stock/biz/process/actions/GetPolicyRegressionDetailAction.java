package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.process.entity.aware.StockRegressionDetailsAware;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.service.StockRegressionDomainService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Action("getPolicyRegressionDetailAction")
public class GetPolicyRegressionDetailAction implements BusinessAction {

    @Resource
    private StockRegressionDomainService stockRegressionDomainService;

    @Override
    public void process(ProcessContext context) {
        StockRegressionDetailsAware entity = context.getEntity();

        String tradePolicyName = entity.getTradePolicyName();

        List<StockRegressionDetail> regressionDetails = stockRegressionDomainService.getRegressionDetails(tradePolicyName);

        entity.setStockRegressionDetails(regressionDetails);
    }
}
