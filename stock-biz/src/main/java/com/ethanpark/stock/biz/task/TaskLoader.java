package com.ethanpark.stock.biz.task;

import com.ethanpark.stock.core.model.TaskConfig;
import com.ethanpark.stock.core.service.TaskDomainService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Service
public class TaskLoader {

    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private TaskConsumer taskConsumer;

    @Resource
    private TaskDomainService taskDomainService;

    @PostConstruct
    private void init() {
        List<TaskConfig> taskConfigs = taskDomainService.getTaskConfigs();

        taskConfigs.forEach(taskConfig -> {
            threadPoolTaskScheduler.schedule(() -> load(taskConfig),
                    new CronTrigger(taskConfig.getCronExpression()));
        });
    }

    public void load(TaskConfig taskConfig) {
        List<Long> taskIds = taskDomainService.selectFireTaskIds(taskConfig.getTaskName(),
                taskConfig.getLimit());

        for (Long taskId : taskIds) {
            threadPoolTaskExecutor.execute(() -> taskConsumer.consume(taskId));
        }
    }
}
