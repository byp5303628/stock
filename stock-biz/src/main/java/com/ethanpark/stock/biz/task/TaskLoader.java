package com.ethanpark.stock.biz.task;

import com.ethanpark.stock.core.model.TaskConfig;
import com.ethanpark.stock.core.service.TaskDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Slf4j
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

    private List<ScheduledFuture<?>> futures = new ArrayList<>();

    @PostConstruct
    private void init() {
        List<TaskConfig> taskConfigs = taskDomainService.getTaskConfigs();

        taskConfigs.forEach(taskConfig -> {
            ScheduledFuture<?> scheduleFuture =
                    threadPoolTaskScheduler.schedule(() -> load(taskConfig),
                    new CronTrigger(taskConfig.getCronExpression()));

            futures.add(scheduleFuture);
        });
    }

    @PreDestroy
    public void destroy() {
        futures.forEach(i -> i.cancel(false));

        threadPoolTaskExecutor.shutdown();
    }

    public void load(TaskConfig taskConfig) {
        log.info("开始进行任务捞取! taskType={}", taskConfig.getTaskType());
        List<Long> taskIds = taskDomainService.selectFireTaskIds(taskConfig.getTaskType(),
                taskConfig.getLimit());

        for (Long taskId : taskIds) {
            threadPoolTaskExecutor.execute(() -> taskConsumer.consume(taskId));
        }
    }
}
