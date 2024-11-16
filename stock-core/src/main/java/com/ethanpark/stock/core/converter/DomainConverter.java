package com.ethanpark.stock.core.converter;

import com.ethanpark.stock.common.dal.mappers.entity.StockBasicDO;
import com.ethanpark.stock.common.dal.mappers.entity.TaskDO;
import com.ethanpark.stock.common.util.JsonUtils;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.model.TaskStatus;
import com.ethanpark.stock.remote.model.StockBasic;

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

    public static StockBasic toDomain(StockBasicDO dbEntity) {

        StockBasic domain = new StockBasic();
        domain.setId(dbEntity.getId());
        domain.setCode(dbEntity.getCode());
        domain.setName(dbEntity.getName());
        domain.setLowestPrice(dbEntity.getLowestPrice());
        domain.setHighestPrice(dbEntity.getHighestPrice());
        domain.setStartPrice(dbEntity.getStartPrice());
        domain.setEndPrice(dbEntity.getEndPrice());
        domain.setPartitionDate(dbEntity.getPartitionDate());
        domain.setTotalValue(dbEntity.getTotalValue());

        domain.setGmtCreate(dbEntity.getGmtCreate());
        domain.setGmtModified(dbEntity.getGmtModified());

        return domain;
    }
}
