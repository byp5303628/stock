package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.TaskMapper;
import com.ethanpark.stock.common.dal.mappers.entity.TaskDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.model.TaskConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
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
        TaskConfig taskConfig = new TaskConfig();

        taskConfig.setTaskType("HfqHistoryRegressionTaskHandler");
        taskConfig.setLimit(20);

        TaskConfig taskConfig2 = new TaskConfig();

        taskConfig2.setTaskType("HistoryStrategyTaskHandler");
        taskConfig2.setLimit(20);

        return Arrays.asList(taskConfig, taskConfig2);
    }

    public List<Long> selectFireTaskIds(String taskName, int limit) {
        return taskMapper.selectFireTaskIds(taskName, limit);
    }

    public Task loadById(Long id) {
        TaskDO taskDO = taskMapper.selectById(id);

        return DomainConverter.toDomain(taskDO);
    }

    public boolean save(Task task) {
        TaskDO taskDO = DbConverter.toDbEntity(task);

        if (task.getId() != null && task.getId() != 0L) {
            return taskMapper.updateById(taskDO) > 0;
        } else {
            int cols = taskMapper.insert(taskDO);
            task.setId(taskDO.getId());

            return cols > 0;
        }
    }
}
