package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import com.ethanpark.stock.biz.process.entity.StrategyDetailRegressionEntity;
import com.ethanpark.stock.core.model.RegressionDetail;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.model.TradePolicyRegression;
import com.ethanpark.stock.core.service.TaskDomainService;
import com.ethanpark.stock.core.service.TradePolicyRegressionDomainService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Action("createTaskAndFlushRegressionAction")
public class CreateTaskAndFlushRegressionAction implements BusinessAction {

    @Resource
    private TaskDomainService taskDomainService;

    @Resource
    private TradePolicyRegressionDomainService tradePolicyRegressionDomainService;

    @Override
    public void process(ProcessContext context) {
        StrategyDetailRegressionEntity entity = context.getEntity();

        List<String> stockCodes = entity.getStockCodes();

        TradePolicyRegression tradePolicyRegression = new TradePolicyRegression();
        tradePolicyRegression.setName(entity.getName());
        tradePolicyRegression.setDetail(new RegressionDetail());
        tradePolicyRegression.getDetail().setAllCodes(new HashSet<>(stockCodes));

        Task task = new Task();

        task.setTaskType("TradePolicyRegressionTaskHandler");
        task.setContext(new HashMap<>());
        task.getContext().put("name", entity.getTradePolicyName());

        boolean save = taskDomainService.save(task);

        if (!save) {
            throw new ProcessException(ErrorCode.SYSTEM_ERROR.getCode(), "保存任务失败!");
        }

        tradePolicyRegression.setTaskId(task.getId());

        Result<Void> result = tradePolicyRegressionDomainService.saveRegression(tradePolicyRegression);

        if (!result.isSuccess()) {
            throw new ProcessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }
    }
}
