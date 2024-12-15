package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.ScheduleConfigMapper;
import com.ethanpark.stock.common.dal.mappers.entity.ScheduleConfigDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.ScheduleConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@Service
public class ScheduleConfigDomainService {

    @Resource
    private ScheduleConfigMapper scheduleConfigMapper;

    public List<ScheduleConfig> getScheduleConfigs() {
        List<ScheduleConfigDO> scheduleConfigDOS = scheduleConfigMapper.selectConfigs();

        return scheduleConfigDOS.stream().map(DomainConverter::toDomain).collect(Collectors.toList());

    }

    public Result<Void> save(ScheduleConfig scheduleConfig) {
        ScheduleConfigDO dbEntity = DbConverter.toDbEntity(scheduleConfig);

        ScheduleConfigDO scheduleConfigDO =
                scheduleConfigMapper.selectByTaskType(dbEntity.getTaskType());

        boolean success = false;
        if (scheduleConfigDO == null) {
            success = scheduleConfigMapper.insert(dbEntity) > 0;
        } else {
            dbEntity.setId(scheduleConfigDO.getId());
            success = scheduleConfigMapper.updateById(dbEntity) > 0;
        }

        if (success) {
            return Result.ok();
        } else {
            return Result.fail("保存失败!");
        }
    }
}
