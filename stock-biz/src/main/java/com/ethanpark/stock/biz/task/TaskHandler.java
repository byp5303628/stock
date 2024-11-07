package com.ethanpark.stock.biz.task;

import com.ethanpark.stock.core.model.Task;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
public interface TaskHandler {
    void handle(Task task);

    String getTaskType();
}
