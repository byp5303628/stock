package com.ethanpark.stock.core.converter;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.common.dal.mappers.entity.StockBasicDO;
import com.ethanpark.stock.common.dal.mappers.entity.TaskDO;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.remote.model.StockBasic;

import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
public class DbConverter {
    public static TaskDO toDbEntity(Task domain) {
        TaskDO dbEntity = new TaskDO();

        dbEntity.setId(domain.getId());
        dbEntity.setTaskType(domain.getTaskType());
        dbEntity.setContext(JSON.toJSONString(domain.getContext()));
        dbEntity.setRetryTimes(domain.getRetryTimes());
        dbEntity.setResultMsg(domain.getResultMsg());
        dbEntity.setStatus(String.valueOf(domain.getStatus()));
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() : domain.getGmtModified());
        dbEntity.setFireTime(domain.getFireTime() == null ? new Date() : domain.getFireTime());

        return dbEntity;
    }

    public static StockBasicDO toDbEntity(StockBasic domain) {

        StockBasicDO dbEntity = new StockBasicDO();
        dbEntity.setId(domain.getId());
        dbEntity.setCode(domain.getCode());
        dbEntity.setName(domain.getName());
        dbEntity.setLowestPrice(domain.getLowestPrice());
        dbEntity.setHighestPrice(domain.getHighestPrice());
        dbEntity.setStartPrice(domain.getStartPrice());
        dbEntity.setEndPrice(domain.getEndPrice());
        dbEntity.setPartitionDate(domain.getPartitionDate());

        return dbEntity;
    }
}
