package com.ethanpark.stock.biz.task.handler;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.biz.task.TaskHandler;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.TaskDomainService;
import com.ethanpark.stock.core.service.TaskStatus;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Slf4j
public abstract class BaseTaskHandler implements TaskHandler {

    @Resource
    protected TaskDomainService taskDomainService;

    @Override
    public void handle(Task task) {
        try {
            Result<Void> result = handle0(task);

            if (result.isSuccess()) {
                dealSuccess(task);
            } else {
                dealFail(task, result.getMsg());
            }
        } catch (Exception e) {
            log.error("执行任务失败! task={}", JSON.toJSONString(task), e);
            dealFail(task, e.getMessage());
        }
    }

    protected abstract Result<Void> handle0(Task task);

    protected void dealSuccess(Task task) {
        task.setStatus(TaskStatus.SUCCESS);
        task.setResultMsg("成功");
        taskDomainService.save(task);
    }

    protected void dealFail(Task task, String errorMsg) {
        if (task.getRetryTimes() > 3) {
            task.setStatus(TaskStatus.FAIL);
            task.setResultMsg(errorMsg);
        } else {
            task.setRetryTimes(task.getRetryTimes() + 1);
            task.setResultMsg(errorMsg);
            task.setStatus(TaskStatus.RETRY);
        }

        taskDomainService.save(task);
    }

    @Override
    public String getTaskType() {
        return this.getClass().getSimpleName();
    }
}
