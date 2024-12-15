package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.ScheduleConfigDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
public interface ScheduleConfigMapper {
    List<ScheduleConfigDO> selectConfigs();

    int insert(ScheduleConfigDO dbEntity);

    int updateById(ScheduleConfigDO dbEntity);

    ScheduleConfigDO selectByTaskType(@Param("taskType") String taskType);
}
