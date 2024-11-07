package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.TaskMapper;
import com.ethanpark.stock.common.entity.TaskDO;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.model.TaskConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Service
public class TaskDomainService {

    @Resource
    private TaskMapper taskMapper;

    public List<TaskConfig> getTaskConfigs() {
        return Collections.emptyList();
    }

    public List<Long> selectFireTaskIds(String taskName, int limit) {
        return taskMapper.selectFireTaskIds(taskName, limit);
    }

    public Task loadById(Long id) {
        TaskDO taskDO = taskMapper.loadById(id);


    }
}
