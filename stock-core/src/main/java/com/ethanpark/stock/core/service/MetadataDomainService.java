package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.metadata.*;

import java.util.List;

/**
 * 元数据领域服务接口。
 *
 * <p>提供元数据模型、字段、枚举的完整 CRUD 操作及 Schema 校验和枚举使用统计能力。
 *
 * @author baiyunpeng04
 */
public interface MetadataDomainService {

    /** 模型管理 */
    List<MetadataModel> listModels();

    MetadataModel getModelById(Long id);

    MetadataModel getModelByCode(String code);

    Result<MetadataModel> saveModel(MetadataModel model);

    void deleteModel(Long id);

    /** 字段管理 */
    MetadataField getFieldById(Long id);

    List<MetadataField> getFieldsByModelId(Long modelId);

    Result<MetadataField> saveField(MetadataField field);

    void deleteField(Long id);

    /** 枚举管理（含使用统计，避免 N+1 查询） */
    List<MetadataEnum> listEnumsWithUsage();

    List<MetadataEnum> listEnums();

    MetadataEnum getEnumById(Long id);

    MetadataEnum getEnumByCode(String code);

    Result<MetadataEnum> saveEnum(MetadataEnum metadataEnum);

    boolean deleteEnum(Long id);

    /** 枚举值管理 */
    List<MetadataEnumValue> getEnumValuesByEnumId(Long enumId);

    Result<MetadataEnumValue> saveEnumValue(MetadataEnumValue value);

    void deleteEnumValue(Long id);

    /** Schema 校验 */
    ValidationResult validateSchema(Long modelId);

    /** 枚举使用统计 */
    EnumUsage getEnumUsage(Long enumId);
}
