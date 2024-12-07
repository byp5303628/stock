package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.core.service.TaskDomainService;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Action("getTradePolicyParentTaskAction")
public class GetTradePolicyParentTaskAction implements BusinessAction {

    @Resource
    private TaskDomainService taskDomainService;

    @Override
    public void process(ProcessContext context) {

    }
}
