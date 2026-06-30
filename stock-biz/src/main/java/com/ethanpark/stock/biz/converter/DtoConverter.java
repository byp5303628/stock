package com.ethanpark.stock.biz.converter;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.biz.dto.*;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.common.util.DateUtils;
import com.ethanpark.stock.core.model.ScheduleConfig;
import com.ethanpark.stock.core.model.TradeCycle;
import com.ethanpark.stock.core.model.metadata.*;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/2
 */
public class DtoConverter {
    public static StrategyDTO toDto(TradePolicy domain) {
        StrategyDTO strategyDTO = new StrategyDTO();
        strategyDTO.setName(domain.getName());
        strategyDTO.setDescription(domain.getDescription());
        strategyDTO.setTags(domain.getStatisticsTypes().stream()
                .map(Enum::name).collect(Collectors.toList()));

        return strategyDTO;
    }

    public static ScheduleConfigDTO toDto(ScheduleConfig scheduleConfig) {

        ScheduleConfigDTO result = new ScheduleConfigDTO();

        BeanUtils.copyProperties(scheduleConfig, result);

        result.setGmtCreate(DateUtils.formatDate(scheduleConfig.getGmtCreate()));
        result.setGmtModified(DateUtils.formatDate(scheduleConfig.getGmtModified()));

        return result;
    }

    public static ScheduleConfig toDomain(ScheduleConfigDTO scheduleConfigDTO) {
        ScheduleConfig result = new ScheduleConfig();

        BeanUtils.copyProperties(scheduleConfigDTO, result);

        return result;
    }

    public static TradeCycleDTO toDto(TradeCycle tradeCycle) {
        TradeCycleDTO tradeCycleDTO = new TradeCycleDTO();

        if (tradeCycle.getSaleLog() == null || tradeCycle.getPurchaseLog() == null) {
            return null;
        }

        tradeCycleDTO.setIncrease(tradeCycle.getIncrease());
        tradeCycleDTO.setPurchaseDate(tradeCycle.getStartDate());
        tradeCycleDTO.setSaleDate(tradeCycle.getEndDate());
        tradeCycleDTO.setPurchasePrice(tradeCycle.getPurchasePrice());
        tradeCycleDTO.setSalePrice(tradeCycle.getSalePrice());
        tradeCycleDTO.setPurchaseDetail(tradeCycle.getPurchaseLog().getStockBasic());
        tradeCycleDTO.setSaleDetail(tradeCycle.getSaleLog().getStockBasic());
        tradeCycleDTO.setGoldDays(DateUtils.dayDiff(tradeCycle.getStartDate(),
                tradeCycle.getEndDate()));

        return tradeCycleDTO;
    }

    // ===== 元数据转换 =====

    /** MetadataModel -> MetadataModelDTO */
    public static MetadataModelDTO toDto(MetadataModel model) {
        if (model == null) {
            return null;
        }
        MetadataModelDTO dto = new MetadataModelDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setCode(model.getCode());
        dto.setModelType(model.getModelType());
        dto.setDescription(model.getDescription());
        dto.setStatus(model.getStatus());
        dto.setCurrentVersion(model.getCurrentVersion());
        dto.setGmtCreate(DateUtils.formatDate(model.getGmtCreate()));
        dto.setGmtModified(DateUtils.formatDate(model.getGmtModified()));

        if (model.getFields() != null) {
            dto.setFields(model.getFields().stream()
                    .map(DtoConverter::toDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /** MetadataModelSaveRequest -> MetadataModel */
    public static MetadataModel toDomain(MetadataModelSaveRequest request) {
        if (request == null) {
            return null;
        }
        MetadataModel model = new MetadataModel();
        model.setId(request.getId());
        model.setName(request.getName());
        model.setCode(request.getCode());
        model.setModelType(request.getModelType());
        model.setDescription(request.getDescription());
        return model;
    }

    /** MetadataField -> MetadataFieldDTO */
    public static MetadataFieldDTO toDto(MetadataField field) {
        if (field == null) {
            return null;
        }
        MetadataFieldDTO dto = new MetadataFieldDTO();
        dto.setId(field.getId());
        dto.setModelId(field.getModelId());
        dto.setFieldName(field.getFieldName());
        dto.setFieldType(field.getFieldType());
        dto.setBusinessMeaning(field.getBusinessMeaning());
        dto.setRequired(field.getRequired());
        dto.setConstraints(field.getConstraints() != null ? JSON.toJSONString(field.getConstraints()) : null);
        dto.setEnumId(field.getEnumId());
        dto.setSortOrder(field.getSortOrder());
        dto.setExtInfo(field.getExtInfo() != null ? JSON.toJSONString(field.getExtInfo()) : null);
        return dto;
    }

    /** MetadataFieldSaveRequest -> MetadataField */
    public static MetadataField toDomain(MetadataFieldSaveRequest request) {
        if (request == null) {
            return null;
        }
        MetadataField field = new MetadataField();
        field.setId(request.getId());
        field.setModelId(request.getModelId());
        field.setFieldName(request.getFieldName());
        field.setFieldType(request.getFieldType());
        field.setBusinessMeaning(request.getBusinessMeaning());
        field.setRequired(request.getRequired());
        field.setEnumId(request.getEnumId());
        field.setSortOrder(request.getSortOrder());
        return field;
    }

    /** MetadataEnum -> MetadataEnumDTO (含引用数) */
    public static MetadataEnumDTO toDto(MetadataEnum metadataEnum) {
        if (metadataEnum == null) {
            return null;
        }
        MetadataEnumDTO dto = new MetadataEnumDTO();
        dto.setId(metadataEnum.getId());
        dto.setName(metadataEnum.getName());
        dto.setCode(metadataEnum.getCode());
        dto.setDescription(metadataEnum.getDescription());
        dto.setStatus(metadataEnum.getStatus());
        dto.setRefModelCount(metadataEnum.getRefModelCount() != null ? metadataEnum.getRefModelCount() : 0);
        dto.setRefFieldCount(metadataEnum.getRefFieldCount() != null ? metadataEnum.getRefFieldCount() : 0);

        if (metadataEnum.getValues() != null) {
            dto.setValues(metadataEnum.getValues().stream()
                    .map(DtoConverter::toDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /** MetadataEnumSaveRequest -> MetadataEnum */
    public static MetadataEnum toDomain(MetadataEnumSaveRequest request) {
        if (request == null) {
            return null;
        }
        MetadataEnum metadataEnum = new MetadataEnum();
        metadataEnum.setId(request.getId());
        metadataEnum.setName(request.getName());
        metadataEnum.setCode(request.getCode());
        metadataEnum.setDescription(request.getDescription());

        if (request.getValues() != null) {
            metadataEnum.setValues(request.getValues().stream()
                    .map(DtoConverter::toDomain)
                    .collect(Collectors.toList()));
        }

        return metadataEnum;
    }

    /** MetadataEnumValue -> MetadataEnumValueDTO */
    public static MetadataEnumValueDTO toDto(MetadataEnumValue value) {
        if (value == null) {
            return null;
        }
        MetadataEnumValueDTO dto = new MetadataEnumValueDTO();
        dto.setId(value.getId());
        dto.setEnumId(value.getEnumId());
        dto.setValueCode(value.getValueCode());
        dto.setValueLabel(value.getValueLabel());
        dto.setSortOrder(value.getSortOrder());
        return dto;
    }

    /** MetadataEnumValueDTO -> MetadataEnumValue */
    public static MetadataEnumValue toDomain(MetadataEnumValueDTO dto) {
        if (dto == null) {
            return null;
        }
        MetadataEnumValue value = new MetadataEnumValue();
        value.setId(dto.getId());
        value.setEnumId(dto.getEnumId());
        value.setValueCode(dto.getValueCode());
        value.setValueLabel(dto.getValueLabel());
        value.setSortOrder(dto.getSortOrder());
        return value;
    }

    /** ValidationResult -> ValidationResultDTO */
    public static ValidationResultDTO toDto(ValidationResult result) {
        if (result == null) {
            return null;
        }
        ValidationResultDTO dto = new ValidationResultDTO();
        dto.setValid(result.isValid());

        if (result.getErrors() != null) {
            dto.setErrors(result.getErrors().stream().map(e -> {
                ValidationErrorDTO errorDTO = new ValidationErrorDTO();
                errorDTO.setField(e.getField());
                errorDTO.setMessage(e.getMessage());
                return errorDTO;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    /** EnumUsage -> EnumUsageDTO */
    public static EnumUsageDTO toDto(EnumUsage usage) {
        if (usage == null) {
            return null;
        }
        EnumUsageDTO dto = new EnumUsageDTO();
        dto.setEnumId(usage.getEnumId());
        dto.setEnumName(usage.getEnumName());
        dto.setTotalRefCount(usage.getTotalRefCount());
        dto.setRefModelCount(usage.getRefModelCount());
        dto.setRefFieldCount(usage.getRefFieldCount());

        if (usage.getRefDetails() != null) {
            List<EnumRefDetailDTO> refDetailDTOs = new ArrayList<>();
            for (EnumUsage.RefDetail detail : usage.getRefDetails()) {
                EnumRefDetailDTO detailDTO = new EnumRefDetailDTO();
                detailDTO.setModelId(detail.getModelId());
                detailDTO.setModelName(detail.getModelName());
                detailDTO.setModelType(detail.getModelType());

                if (detail.getFields() != null) {
                    detailDTO.setFields(detail.getFields().stream().map(f -> {
                        RefFieldDTO fieldDTO = new RefFieldDTO();
                        fieldDTO.setFieldId(f.getFieldId());
                        fieldDTO.setFieldName(f.getFieldName());
                        return fieldDTO;
                    }).collect(Collectors.toList()));
                }

                refDetailDTOs.add(detailDTO);
            }
            dto.setRefDetails(refDetailDTOs);
        }

        return dto;
    }

    /** MetadataModelVersion -> ModelVersionDTO */
    public static ModelVersionDTO toDto(MetadataModelVersion version) {
        if (version == null) {
            return null;
        }
        ModelVersionDTO dto = new ModelVersionDTO();
        dto.setId(version.getId());
        dto.setVersion(version.getVersion());
        dto.setVersionDesc(version.getVersionDesc());
        dto.setGmtCreate(DateUtils.formatDate(version.getGmtCreate()));
        // isCurrent set by Controller
        return dto;
    }
}