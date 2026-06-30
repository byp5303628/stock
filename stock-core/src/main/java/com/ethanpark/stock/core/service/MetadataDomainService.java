package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.metadata.*;

import java.util.List;
import java.util.Map;

/**
 * 元数据领域服务接口。
 *
 * <p>提供元数据模型、字段、枚举的完整 CRUD 操作及 Schema 校验、版本管理和枚举使用统计能力。
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

    /**
     * 发布模型，生成版本快照。
     *
     * @param modelId    模型 ID
     * @param versionDesc 版本说明（可选）
     */
    void publishModel(Long modelId, String versionDesc);

    /** 生成 JSON Schema */
    Map<String, Object> generateJsonSchema(Long modelId);

    /** 枚举使用统计 */
    EnumUsage getEnumUsage(Long enumId);

    /** ===== 版本管理 ===== */

    /**
     * 查询模型版本列表。
     *
     * @param modelId 模型 ID
     * @return 版本列表，按版本号降序排列
     */
    List<MetadataModelVersion> listModelVersions(Long modelId);

    /**
     * 切换当前生效版本。
     *
     * @param modelId 模型 ID
     * @param version 目标版本号
     */
    void switchModelVersion(Long modelId, Integer version);

    /**
     * 查询指定版本的 JSONSchema。
     *
     * @param modelId 模型 ID
     * @param version 版本号
     * @return JSONSchema 内容
     */
    Map<String, Object> getSchemaByVersion(Long modelId, Integer version);

    /**
     * 获取模型当前生效的 JSONSchema。
     *
     * <p>优先读 DB 缓存，无缓存时 fallback 实时计算。
     *
     * @param modelId 模型 ID
     * @return JSONSchema 内容
     */
    Map<String, Object> getModelSchema(Long modelId);
}