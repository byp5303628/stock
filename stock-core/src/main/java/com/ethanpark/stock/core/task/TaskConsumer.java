package com.ethanpark.stock.core.task;

import com.ethanpark.stock.core.service.TaskDomainService;
import org.springframework.stereotype.Service;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Service
public class TaskConsumer {

    private TaskDomainService taskDomainService;

    public void consume(Long taskId) {
        taskDomainService.lo
    }
}
