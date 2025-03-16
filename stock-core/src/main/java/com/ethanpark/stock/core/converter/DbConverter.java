package com.ethanpark.stock.core.converter;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.common.dal.mappers.entity.*;
import com.ethanpark.stock.core.model.*;
import com.ethanpark.stock.remote.model.StockBasic;

import java.time.LocalDateTime;
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
        dbEntity.setExternalSerialNo(domain.getExternalSerialNo());
        dbEntity.setRetryTimes(domain.getRetryTimes());
        dbEntity.setResultMsg(domain.getResultMsg() == null || domain.getResultMsg().length() < 256 ? domain.getResultMsg() : domain.getResultMsg().substring(0, 256));
        dbEntity.setStatus(String.valueOf(domain.getStatus()));
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? LocalDateTime.now(): domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? LocalDateTime.now() :
                domain.getGmtModified());
        dbEntity.setFireTime(domain.getFireTime() == null ? LocalDateTime.now() : domain.getFireTime());

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
        dbEntity.setTotalValue(domain.getTotalValue());

        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() :
                domain.getGmtModified());

        return dbEntity;
    }

    public static StockStatisticsDO toDbEntity(StockStatistics domain) {
        StockStatisticsDO dbEntity = new StockStatisticsDO();

        dbEntity.setId(domain.getId());
        dbEntity.setCode(domain.getCode());
        dbEntity.setPartitionDate(domain.getPartitionDate());
        dbEntity.setStatisticsType(domain.getStatisticsType().name());
        dbEntity.setStatisticsName(domain.getStatisticsName());
        dbEntity.setStatistics(JSON.toJSONString(domain.getStatistics()));

        return dbEntity;
    }

    public static TradePolicyRegressionDO toDbEntity(TradePolicyRegression regression) {
        TradePolicyRegressionDO tradePolicyRegressionDO = new TradePolicyRegressionDO();

        tradePolicyRegressionDO.setName(regression.getName());
        tradePolicyRegressionDO.setId(regression.getId());
        tradePolicyRegressionDO.setDetail(JSON.toJSONString(regression.getDetail()));
        tradePolicyRegressionDO.setTaskId(regression.getTaskId());

        return tradePolicyRegressionDO;
    }

    public static StockRegressionDetailDO toDbEntity(StockRegressionDetail domain) {
        StockRegressionDetailDO result = new StockRegressionDetailDO();

        result.setId(domain.getId());
        result.setCode(domain.getCode());
        result.setTradePolicyName(domain.getTradePolicyName());
        result.setTradeCycles(JSON.toJSONString(domain.getTradeCycles()));

        return result;
    }

    public static ScheduleConfigDO toDbEntity(ScheduleConfig domain) {
        ScheduleConfigDO dbEntity = new ScheduleConfigDO();

        dbEntity.setCronExpression(domain.getCronExpression());
        dbEntity.setTaskType(domain.getTaskType());
        dbEntity.setCount(domain.getCount());
        dbEntity.setId(domain.getId());
        dbEntity.setStatus(domain.getStatus());

        dbEntity.setGmtCreate(domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified());

        return dbEntity;
    }
}
