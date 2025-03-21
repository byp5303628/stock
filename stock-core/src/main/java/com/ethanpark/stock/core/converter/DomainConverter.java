package com.ethanpark.stock.core.converter;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.common.dal.mappers.entity.*;
import com.ethanpark.stock.common.util.JsonUtils;
import com.ethanpark.stock.core.model.*;
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

    public static StockStatistics toDomain(StockStatisticsDO dbEntity) {
        StockStatistics domain = new StockStatistics();

        domain.setId(dbEntity.getId());
        domain.setCode(dbEntity.getCode());
        domain.setPartitionDate(dbEntity.getPartitionDate());
        domain.setStatisticsType(StatisticsType.getByName(dbEntity.getStatisticsType()));
        domain.setStatisticsName(dbEntity.getStatisticsName());
        domain.setStatistics(JsonUtils.toBigDecimalMap(dbEntity.getStatistics()));

        return domain;
    }

    public static TradePolicyRegression toDomain(TradePolicyRegressionDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        TradePolicyRegression regression = new TradePolicyRegression();

        regression.setId(dbEntity.getId());
        regression.setDetail(JSON.parseObject(dbEntity.getDetail(), RegressionDetail.class));
        regression.setTaskId(dbEntity.getTaskId());
        regression.setName(dbEntity.getName());

        return regression;
    }

    public static ScheduleConfig toDomain(ScheduleConfigDO dbEntity) {
        ScheduleConfig result = new ScheduleConfig();

        result.setCronExpression(dbEntity.getCronExpression());
        result.setTaskType(dbEntity.getTaskType());
        result.setCount(dbEntity.getCount());
        result.setId(dbEntity.getId());
        result.setStatus(dbEntity.getStatus());

        result.setGmtCreate(dbEntity.getGmtCreate());
        result.setGmtModified(dbEntity.getGmtModified());

        return result;
    }

    public static StockRegressionDetail toDomain(StockRegressionDetailDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        StockRegressionDetail result = new StockRegressionDetail();

        result.setId(dbEntity.getId());
        result.setCode(dbEntity.getCode());
        result.setTradePolicyName(dbEntity.getTradePolicyName());
        result.setTradeCycles(JSON.parseArray(dbEntity.getTradeCycles(), TradeCycle.class));

        return result;
    }
}
