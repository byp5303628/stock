package com.ethanpark.stock.core.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ethanpark.stock.common.dal.mappers.entity.*;
import com.ethanpark.stock.common.util.JsonUtils;
import com.ethanpark.stock.core.model.*;
import com.ethanpark.stock.core.model.metadata.MetadataEnum;
import com.ethanpark.stock.core.model.metadata.MetadataEnumValue;
import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.model.metadata.MetadataModelVersion;
import com.ethanpark.stock.remote.model.StockBasic;

import java.util.Map;

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

    /**
     * 将 MetadataModelDO 转换为 MetadataModel 领域对象。
     */
    public static MetadataModel toDomain(MetadataModelDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        MetadataModel domain = new MetadataModel();
        domain.setId(dbEntity.getId());
        domain.setName(dbEntity.getName());
        domain.setCode(dbEntity.getCode());
        domain.setModelType(dbEntity.getModelType());
        domain.setDescription(dbEntity.getDescription());
        domain.setStatus(dbEntity.getStatus());
        domain.setCurrentVersion(dbEntity.getCurrentVersion());
        domain.setSnapshotHash(dbEntity.getSnapshotHash());
        domain.setExtInfo(parseExtInfo(dbEntity.getExtInfo()));
        domain.setGmtCreate(dbEntity.getGmtCreate());
        domain.setGmtModified(dbEntity.getGmtModified());

        return domain;
    }

    /**
     * 将 MetadataFieldDO 转换为 MetadataField 领域对象。
     */
    public static MetadataField toDomain(MetadataFieldDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        MetadataField domain = new MetadataField();
        domain.setId(dbEntity.getId());
        domain.setModelId(dbEntity.getModelId());
        domain.setFieldName(dbEntity.getFieldName());
        domain.setFieldType(dbEntity.getFieldType());
        domain.setBusinessMeaning(dbEntity.getBusinessMeaning());
        domain.setRequired(dbEntity.getRequired() != null && dbEntity.getRequired() == 1);
        domain.setConstraints(parseExtInfo(dbEntity.getConstraints()));
        domain.setEnumId(dbEntity.getEnumId());
        domain.setSortOrder(dbEntity.getSortOrder());
        domain.setExtInfo(parseExtInfo(dbEntity.getExtInfo()));
        domain.setGmtCreate(dbEntity.getGmtCreate());
        domain.setGmtModified(dbEntity.getGmtModified());

        return domain;
    }

    /**
     * 将 MetadataEnumDO 转换为 MetadataEnum 领域对象。
     */
    public static MetadataEnum toDomain(MetadataEnumDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        MetadataEnum domain = new MetadataEnum();
        domain.setId(dbEntity.getId());
        domain.setName(dbEntity.getName());
        domain.setCode(dbEntity.getCode());
        domain.setDescription(dbEntity.getDescription());
        domain.setStatus(dbEntity.getStatus());
        domain.setGmtCreate(dbEntity.getGmtCreate());
        domain.setGmtModified(dbEntity.getGmtModified());

        return domain;
    }

    /**
     * 将 MetadataEnumValueDO 转换为 MetadataEnumValue 领域对象。
     */
    public static MetadataEnumValue toDomain(MetadataEnumValueDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        MetadataEnumValue domain = new MetadataEnumValue();
        domain.setId(dbEntity.getId());
        domain.setEnumId(dbEntity.getEnumId());
        domain.setValueCode(dbEntity.getValueCode());
        domain.setValueLabel(dbEntity.getValueLabel());
        domain.setSortOrder(dbEntity.getSortOrder());
        domain.setExtInfo(parseExtInfo(dbEntity.getExtInfo()));
        domain.setGmtCreate(dbEntity.getGmtCreate());
        domain.setGmtModified(dbEntity.getGmtModified());

        return domain;
    }

    /**
     * 将 MetadataModelVersionDO 转换为 MetadataModelVersion 领域对象。
     */
    public static MetadataModelVersion toDomain(MetadataModelVersionDO dbEntity) {
        if (dbEntity == null) {
            return null;
        }

        MetadataModelVersion domain = new MetadataModelVersion();
        domain.setId(dbEntity.getId());
        domain.setModelCode(dbEntity.getModelCode());
        domain.setModelId(dbEntity.getModelId());
        domain.setVersion(dbEntity.getVersion());
        if (dbEntity.getSchemaContent() != null && !dbEntity.getSchemaContent().isEmpty()) {
            domain.setSchemaContent(JSON.parseObject(dbEntity.getSchemaContent(),
                    new TypeReference<Map<String, Object>>() {}));
        }
        domain.setVersionDesc(dbEntity.getVersionDesc());
        domain.setGmtCreate(dbEntity.getGmtCreate());
        domain.setGmtModified(dbEntity.getGmtModified());

        return domain;
    }

    private static Map<String, Object> parseExtInfo(String extInfo) {
        if (extInfo == null || extInfo.isEmpty()) {
            return null;
        }
        return JSON.parseObject(extInfo, new TypeReference<Map<String, Object>>() {});
    }
}
