package com.ethanpark.stock.core.converter;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.common.entity.StockBasicDO;
import com.ethanpark.stock.common.entity.TaskDO;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.remote.model.StockBasic;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
public class DbConverter {
    public static TaskDO toDbEntity(Task domain) {
        TaskDO dbEntity = new TaskDO();

        dbEntity.setId(domain.getId());
        dbEntity.setContext(JSON.toJSONString(domain.getContext()));
        dbEntity.setStatus(String.valueOf(domain.getStatus()));
        dbEntity.setGmtCreate(domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified());
        dbEntity.setFireTime(domain.getFireTime());

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
