package com.ethanpark.stock.biz.task;

import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.TaskDomainService;
import com.ethanpark.stock.core.model.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Slf4j
@Service
public class TaskConsumer {

    @Resource
    private TaskDomainService taskDomainService;

    @Resource
    private TaskHandlerFactory taskHandlerFactory;

    public void consume(Long taskId) {
        Task task = taskDomainService.loadById(taskId);

        if (task == null || (task.getStatus() != TaskStatus.INIT && task.getStatus() != TaskStatus.RETRY)) {
            log.info("状态不正确, 无需消费, taskId={}", taskId);
            return;
        }

        log.info("开始进行任务处理! id={}", taskId);

        task.setStatus(TaskStatus.PROCESSING);

        taskDomainService.save(task);

        TaskHandler taskHandler = taskHandlerFactory.selectHandler(task.getTaskType());

        taskHandler.handle(task);

        log.info("任务处理完成! id={},", taskId);
    }
}
