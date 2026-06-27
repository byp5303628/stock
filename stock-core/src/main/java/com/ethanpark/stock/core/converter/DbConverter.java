package com.ethanpark.stock.core.converter;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.common.dal.mappers.entity.*;
import com.ethanpark.stock.core.model.*;
import com.ethanpark.stock.core.model.metadata.MetadataEnum;
import com.ethanpark.stock.core.model.metadata.MetadataEnumValue;
import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.model.metadata.MetadataModelVersion;
import com.ethanpark.stock.remote.model.StockBasic;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

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

    /**
     * 将 MetadataModel 领域对象转换为 MetadataModelDO 持久化对象。
     */
    public static MetadataModelDO toDbEntity(MetadataModel domain) {
        if (domain == null) {
            return null;
        }

        MetadataModelDO dbEntity = new MetadataModelDO();
        dbEntity.setId(domain.getId());
        dbEntity.setName(domain.getName());
        dbEntity.setCode(domain.getCode());
        dbEntity.setModelType(domain.getModelType());
        dbEntity.setDescription(domain.getDescription());
        dbEntity.setStatus(domain.getStatus() == null ? "DRAFT" : domain.getStatus());
        dbEntity.setCurrentVersion(domain.getCurrentVersion() == null ? 0 : domain.getCurrentVersion());
        dbEntity.setSnapshotHash(domain.getSnapshotHash());
        dbEntity.setExtInfo(toJsonString(domain.getExtInfo()));
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() : domain.getGmtModified());

        return dbEntity;
    }

    /**
     * 将 MetadataField 领域对象转换为 MetadataFieldDO 持久化对象。
     */
    public static MetadataFieldDO toDbEntity(MetadataField domain) {
        if (domain == null) {
            return null;
        }

        MetadataFieldDO dbEntity = new MetadataFieldDO();
        dbEntity.setId(domain.getId());
        dbEntity.setModelId(domain.getModelId());
        dbEntity.setFieldName(domain.getFieldName());
        dbEntity.setFieldType(domain.getFieldType());
        dbEntity.setBusinessMeaning(domain.getBusinessMeaning() == null ? "" : domain.getBusinessMeaning());
        dbEntity.setRequired(domain.getRequired() != null && domain.getRequired() ? 1 : 0);
        dbEntity.setConstraints(toJsonString(domain.getConstraints()));
        dbEntity.setEnumId(domain.getEnumId());
        dbEntity.setSortOrder(domain.getSortOrder() == null ? 0 : domain.getSortOrder());
        dbEntity.setExtInfo(toJsonString(domain.getExtInfo()));
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() : domain.getGmtModified());

        return dbEntity;
    }

    /**
     * 将 MetadataEnum 领域对象转换为 MetadataEnumDO 持久化对象。
     */
    public static MetadataEnumDO toDbEntity(MetadataEnum domain) {
        if (domain == null) {
            return null;
        }

        MetadataEnumDO dbEntity = new MetadataEnumDO();
        dbEntity.setId(domain.getId());
        dbEntity.setName(domain.getName());
        dbEntity.setCode(domain.getCode());
        dbEntity.setDescription(domain.getDescription());
        dbEntity.setStatus(domain.getStatus() == null ? "ENABLED" : domain.getStatus());
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() : domain.getGmtModified());

        return dbEntity;
    }

    /**
     * 将 MetadataEnumValue 领域对象转换为 MetadataEnumValueDO 持久化对象。
     */
    public static MetadataEnumValueDO toDbEntity(MetadataEnumValue domain) {
        if (domain == null) {
            return null;
        }

        MetadataEnumValueDO dbEntity = new MetadataEnumValueDO();
        dbEntity.setId(domain.getId());
        dbEntity.setEnumId(domain.getEnumId());
        dbEntity.setValueCode(domain.getValueCode());
        dbEntity.setValueLabel(domain.getValueLabel());
        dbEntity.setSortOrder(domain.getSortOrder() == null ? 0 : domain.getSortOrder());
        dbEntity.setExtInfo(toJsonString(domain.getExtInfo()));
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() : domain.getGmtModified());

        return dbEntity;
    }

    /**
     * 将 MetadataModelVersion 领域对象转换为 MetadataModelVersionDO 持久化对象。
     *
     * <p>NOT NULL 默认值（与 basic_init.sql 对齐）：
     * <ul>
     *   <li>versionDesc → ""</li>
     *   <li>schemaContent → null（允许 null）</li>
     * </ul>
     */
    public static MetadataModelVersionDO toDbEntity(MetadataModelVersion domain) {
        if (domain == null) {
            return null;
        }

        MetadataModelVersionDO dbEntity = new MetadataModelVersionDO();
        dbEntity.setId(domain.getId());
        dbEntity.setModelCode(domain.getModelCode());
        dbEntity.setModelId(domain.getModelId());
        dbEntity.setVersion(domain.getVersion());
        dbEntity.setSchemaContent(domain.getSchemaContent() != null
                ? JSON.toJSONString(domain.getSchemaContent()) : null);
        dbEntity.setVersionDesc(domain.getVersionDesc() == null ? "" : domain.getVersionDesc());
        dbEntity.setGmtCreate(domain.getGmtCreate() == null ? new Date() : domain.getGmtCreate());
        dbEntity.setGmtModified(domain.getGmtModified() == null ? new Date() : domain.getGmtModified());

        return dbEntity;
    }

    private static String toJsonString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(map);
    }
}
