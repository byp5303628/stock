package com.ethanpark.stock.core.converter;

import com.ethanpark.stock.common.entity.TaskDO;
import com.ethanpark.stock.common.util.JsonUtils;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.TaskStatus;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
public class DomainConverter {
    public static Task toDomain(TaskDO taskDo) {
        if (taskDo == null) {
            return null;
        }

        Task domain = new Task();

        domain.setId(taskDo.getId());
        domain.setContext(JsonUtils.toStringMap(taskDo.getContext()));
        domain.setStatus(TaskStatus.valueOf(taskDo.getStatus()));
        domain.setTaskType(taskDo.getTaskType());
        domain.setGmtCreate(taskDo.getGmtCreate());
        domain.setGmtModified(taskDo.getGmtModified());
        domain.setFireTime(taskDo.getFireTime());

        return domain;
    }
}
