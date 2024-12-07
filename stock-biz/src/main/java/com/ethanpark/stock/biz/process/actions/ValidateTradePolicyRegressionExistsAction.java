package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import com.ethanpark.stock.biz.process.entity.StrategyDetailRegressionEntity;
import com.ethanpark.stock.core.model.TradePolicyRegression;
import com.ethanpark.stock.core.service.TradePolicyRegressionDomainService;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Action("validateTradePolicyRegressionExistsAction")
public class ValidateTradePolicyRegressionExistsAction implements BusinessAction {

    @Resource
    private TradePolicyRegressionDomainService tradePolicyRegressionDomainService;


    @Override
    public void process(ProcessContext context) {
        StrategyDetailRegressionEntity entity = context.getEntity();

        String tradePolicyName = entity.getTradePolicyName();

        TradePolicyRegression regression =
                tradePolicyRegressionDomainService.loadByName(tradePolicyName);

        if (regression != null) {
            throw new ProcessException(ErrorCode.ILLEGAL_PARAM.getCode(), "已经存在对应的回溯任务, 无需重新执行回溯");
        }
    }
}
