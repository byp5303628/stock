package com.ethanpark.stock.common.dal;

import com.ethanpark.stock.common.entity.TaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Mapper
public interface TaskMapper {
    List<Long> selectFireTaskIds(String name, int limit);

    int insert(TaskDO taskDO);

    int updateById(TaskDO taskDO);

    TaskDO selectById(Long taskId);
}
