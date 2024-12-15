package com.ethanpark.stock.biz.task;

import com.ethanpark.stock.core.model.ScheduleConfig;
import com.ethanpark.stock.core.service.ScheduleConfigDomainService;
import com.ethanpark.stock.core.service.TaskDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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

    private ScheduledFuture<?> future;

    @Resource
    private ScheduleConfigDomainService scheduleConfigDomainService;

    @PostConstruct
    private void init() {
        ScheduledFuture<?> scheduleFuture =
                threadPoolTaskScheduler.schedule(() -> load(),
                        new CronTrigger("* * * * * ?"));

        future = scheduleFuture;
    }

    @PreDestroy
    public void destroy() {
        if (future != null) {
            future.cancel(true);
        }

        threadPoolTaskExecutor.shutdown();
    }


    public void load() {
        List<ScheduleConfig> scheduleConfigs = scheduleConfigDomainService.getScheduleConfigs();

        for (ScheduleConfig scheduleConfig : scheduleConfigs) {
            if (!Objects.equals(scheduleConfig.getStatus(), "T")) {
                continue;
            }

            log.info("开始进行任务捞取! taskType={}", scheduleConfig.getTaskType());
            List<Long> taskIds = taskDomainService.selectFireTaskIds(scheduleConfig.getTaskType(),
                    scheduleConfig.getCount());

            for (Long taskId : taskIds) {
                threadPoolTaskExecutor.execute(() -> taskConsumer.consume(taskId));
            }
        }
    }
}
