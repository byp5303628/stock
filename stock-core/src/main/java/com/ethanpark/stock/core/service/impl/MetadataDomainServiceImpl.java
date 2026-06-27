package com.ethanpark.stock.core.service.impl;

import com.ethanpark.stock.common.dal.mappers.MetadataEnumMapper;
import com.ethanpark.stock.common.dal.mappers.MetadataEnumValueMapper;
import com.ethanpark.stock.common.dal.mappers.MetadataFieldMapper;
import com.ethanpark.stock.common.dal.mappers.MetadataModelMapper;
import com.ethanpark.stock.common.dal.mappers.entity.MetadataEnumDO;
import com.ethanpark.stock.common.dal.mappers.entity.MetadataEnumValueDO;
import com.ethanpark.stock.common.dal.mappers.entity.MetadataFieldDO;
import com.ethanpark.stock.common.dal.mappers.entity.MetadataModelDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.metadata.*;
import com.ethanpark.stock.core.service.MetadataDomainService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 元数据领域服务实现。
 *
 * @author baiyunpeng04
 */
@Service
public class MetadataDomainServiceImpl implements MetadataDomainService {

    private static final Set<String> VALID_FIELD_TYPES = new HashSet<>(Arrays.asList(
            "STRING", "NUMBER", "INTEGER", "DECIMAL", "BOOLEAN", "ENUM", "DATE", "DATETIME"));

    private static final String CACHE_MODELS = "metadataModels";
    private static final String CACHE_ENUMS = "metadataEnums";
    private static final String CACHE_KEY_ALL = "'all'";

    private static final String STATUS_DELETED = "DELETED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    // H5: 构造器注入替代 @Resource 字段注入
    private final MetadataModelMapper metadataModelMapper;
    private final MetadataFieldMapper metadataFieldMapper;
    private final MetadataEnumMapper metadataEnumMapper;
    private final MetadataEnumValueMapper metadataEnumValueMapper;
    private final CacheManager cacheManager;

    public MetadataDomainServiceImpl(
            MetadataModelMapper metadataModelMapper,
            MetadataFieldMapper metadataFieldMapper,
            MetadataEnumMapper metadataEnumMapper,
            MetadataEnumValueMapper metadataEnumValueMapper,
            CacheManager cacheManager) {
        this.metadataModelMapper = metadataModelMapper;
        this.metadataFieldMapper = metadataFieldMapper;
        this.metadataEnumMapper = metadataEnumMapper;
        this.metadataEnumValueMapper = metadataEnumValueMapper;
        this.cacheManager = cacheManager;
    }

    // ===== 模型管理 =====

    @Override
    @Cacheable(value = CACHE_MODELS, key = CACHE_KEY_ALL)
    public List<MetadataModel> listModels() {
        List<MetadataModelDO> models = metadataModelMapper.selectAll();
        return models.stream().map(DomainConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public MetadataModel getModelById(Long id) {
        MetadataModelDO modelDO = metadataModelMapper.selectById(id);
        return DomainConverter.toDomain(modelDO);
    }

    @Override
    public MetadataModel getModelByCode(String code) {
        MetadataModelDO modelDO = metadataModelMapper.selectByCode(code);
        return DomainConverter.toDomain(modelDO);
    }

    @Override
    @CacheEvict(value = CACHE_MODELS, key = CACHE_KEY_ALL)
    public Result<MetadataModel> saveModel(MetadataModel model) {
        MetadataModelDO dbEntity = DbConverter.toDbEntity(model);

        if (dbEntity.getId() == null || dbEntity.getId() == 0L) {
            // 新增前检查 code 唯一性
            MetadataModelDO existing = metadataModelMapper.selectByCode(dbEntity.getCode());
            if (existing != null) {
                return Result.fail("模型编码 " + dbEntity.getCode() + " 已存在");
            }
            // H2: 捕获并发情况下的 DuplicateKeyException
            try {
                metadataModelMapper.insert(dbEntity);
            } catch (DuplicateKeyException e) {
                return Result.fail("模型编码 " + dbEntity.getCode() + " 已存在");
            }
            model.setId(dbEntity.getId());
        } else {
            metadataModelMapper.updateById(dbEntity);
        }

        MetadataModel saved = DomainConverter.toDomain(metadataModelMapper.selectById(dbEntity.getId()));
        return Result.ok(saved);
    }

    @Override
    @CacheEvict(value = CACHE_MODELS, key = CACHE_KEY_ALL)
    @Transactional  // C1: 添加事务保护
    public void deleteModel(Long id) {
        metadataFieldMapper.deleteByModelId(id);
        metadataModelMapper.deleteById(id);
    }

    // ===== 字段管理 =====

    @Override
    public MetadataField getFieldById(Long id) {
        MetadataFieldDO fieldDO = metadataFieldMapper.selectById(id);
        return DomainConverter.toDomain(fieldDO);
    }

    @Override
    public List<MetadataField> getFieldsByModelId(Long modelId) {
        List<MetadataFieldDO> fields = metadataFieldMapper.selectByModelId(modelId);
        return fields.stream().map(DomainConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = CACHE_MODELS, key = CACHE_KEY_ALL)
    public Result<MetadataField> saveField(MetadataField field) {
        // H3: fieldType 合法性校验
        if (field.getFieldType() != null && !VALID_FIELD_TYPES.contains(field.getFieldType())) {
            return Result.fail("fieldType 不合法: " + field.getFieldType());
        }

        MetadataFieldDO dbEntity = DbConverter.toDbEntity(field);

        if (dbEntity.getId() == null || dbEntity.getId() == 0L) {
            metadataFieldMapper.insert(dbEntity);
            field.setId(dbEntity.getId());
        } else {
            metadataFieldMapper.updateById(dbEntity);
        }

        MetadataField saved = DomainConverter.toDomain(metadataFieldMapper.selectById(dbEntity.getId()));
        return Result.ok(saved);
    }

    @Override
    @CacheEvict(value = CACHE_MODELS, key = CACHE_KEY_ALL)
    public void deleteField(Long id) {
        // H4: 先检查字段是否存在，不存在则静默成功
        MetadataField existing = getFieldById(id);
        if (existing == null) {
            return;
        }
        metadataFieldMapper.deleteById(id);
    }

    // ===== 枚举管理 =====

    @Override
    @Cacheable(value = CACHE_ENUMS, key = CACHE_KEY_ALL)
    public List<MetadataEnum> listEnums() {
        List<MetadataEnumDO> enumDOs = metadataEnumMapper.selectAll();
        if (enumDOs.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询枚举值（避免 N+1）
        List<Long> enumIds = enumDOs.stream().map(MetadataEnumDO::getId).collect(Collectors.toList());
        List<MetadataEnumValueDO> allValues = metadataEnumValueMapper.selectByEnumIds(enumIds);
        Map<Long, List<MetadataEnumValueDO>> valuesByEnumId = allValues.stream()
                .collect(Collectors.groupingBy(MetadataEnumValueDO::getEnumId));

        List<MetadataEnum> result = new ArrayList<>();
        for (MetadataEnumDO enumDO : enumDOs) {
            MetadataEnum metadataEnum = DomainConverter.toDomain(enumDO);
            List<MetadataEnumValueDO> valueDOs = valuesByEnumId.getOrDefault(enumDO.getId(), Collections.emptyList());
            List<MetadataEnumValue> values = valueDOs.stream()
                    .map(DomainConverter::toDomain)
                    .collect(Collectors.toList());
            metadataEnum.setValues(values);
            result.add(metadataEnum);
        }

        return result;
    }

    @Override
    public List<MetadataEnum> listEnumsWithUsage() {
        List<MetadataEnumDO> enumDOs = metadataEnumMapper.selectAll();
        if (enumDOs.isEmpty()) {
            return Collections.emptyList();
        }

        // C3: 批量收集所有 enumId，一次性批量查询，将 2N+1 降为 3 次查询
        List<Long> enumIds = enumDOs.stream()
                .map(MetadataEnumDO::getId)
                .collect(Collectors.toList());

        // 批量查询所有枚举值（按 enumId 分组）
        List<MetadataEnumValueDO> allValues = metadataEnumValueMapper.selectByEnumIds(enumIds);
        Map<Long, List<MetadataEnumValueDO>> valuesByEnumId = allValues.stream()
                .collect(Collectors.groupingBy(MetadataEnumValueDO::getEnumId));

        // 批量查询所有引用字段（按 enumId 分组）
        List<MetadataFieldDO> allRefFields = metadataFieldMapper.selectByEnumIds(enumIds);
        Map<Long, List<MetadataFieldDO>> refFieldsByEnumId = allRefFields.stream()
                .collect(Collectors.groupingBy(MetadataFieldDO::getEnumId));

        // 组装结果
        List<MetadataEnum> result = new ArrayList<>();
        for (MetadataEnumDO enumDO : enumDOs) {
            MetadataEnum metadataEnum = DomainConverter.toDomain(enumDO);

            // 填充枚举值
            List<MetadataEnumValueDO> valueDOs = valuesByEnumId.getOrDefault(enumDO.getId(), Collections.emptyList());
            metadataEnum.setValues(valueDOs.stream()
                    .map(DomainConverter::toDomain)
                    .collect(Collectors.toList()));

            // 填充引用统计
            List<MetadataFieldDO> refFields = refFieldsByEnumId.getOrDefault(enumDO.getId(), Collections.emptyList());
            long distinctModelCount = refFields.stream()
                    .map(MetadataFieldDO::getModelId)
                    .distinct()
                    .count();
            metadataEnum.setRefModelCount((int) distinctModelCount);
            metadataEnum.setRefFieldCount(refFields.size());

            result.add(metadataEnum);
        }

        return result;
    }

    @Override
    public MetadataEnum getEnumById(Long id) {
        MetadataEnumDO enumDO = metadataEnumMapper.selectById(id);
        if (enumDO == null) {
            return null;
        }
        MetadataEnum metadataEnum = DomainConverter.toDomain(enumDO);

        List<MetadataEnumValueDO> valueDOs = metadataEnumValueMapper.selectByEnumId(id);
        List<MetadataEnumValue> values = valueDOs.stream()
                .map(DomainConverter::toDomain)
                .collect(Collectors.toList());
        metadataEnum.setValues(values);

        return metadataEnum;
    }

    @Override
    public MetadataEnum getEnumByCode(String code) {
        MetadataEnumDO enumDO = metadataEnumMapper.selectByCode(code);
        if (enumDO == null) {
            return null;
        }
        MetadataEnum metadataEnum = DomainConverter.toDomain(enumDO);

        List<MetadataEnumValueDO> valueDOs = metadataEnumValueMapper.selectByEnumId(enumDO.getId());
        List<MetadataEnumValue> values = valueDOs.stream()
                .map(DomainConverter::toDomain)
                .collect(Collectors.toList());
        metadataEnum.setValues(values);

        return metadataEnum;
    }

    @Override
    @CacheEvict(value = CACHE_ENUMS, key = CACHE_KEY_ALL)
    @Transactional
    public Result<MetadataEnum> saveEnum(MetadataEnum metadataEnum) {
        // 1. 保存枚举定义
        MetadataEnumDO dbEntity = DbConverter.toDbEntity(metadataEnum);
        if (dbEntity.getId() == null || dbEntity.getId() == 0L) {
            // 新增前检查 code 唯一性
            MetadataEnumDO existing = metadataEnumMapper.selectByCode(dbEntity.getCode());
            if (existing != null) {
                return Result.fail("枚举编码 " + dbEntity.getCode() + " 已存在");
            }
            // H2: 捕获并发情况下的 DuplicateKeyException
            try {
                metadataEnumMapper.insert(dbEntity);
            } catch (DuplicateKeyException e) {
                return Result.fail("枚举编码 " + dbEntity.getCode() + " 已存在");
            }
            metadataEnum.setId(dbEntity.getId());
        } else {
            metadataEnumMapper.updateById(dbEntity);
        }

        // 2. 处理枚举值（先删后插，在事务中保证原子性）
        if (metadataEnum.getValues() != null) {
            metadataEnumValueMapper.deleteByEnumId(dbEntity.getId());
            for (MetadataEnumValue value : metadataEnum.getValues()) {
                value.setEnumId(dbEntity.getId());
                MetadataEnumValueDO valueDO = DbConverter.toDbEntity(value);
                metadataEnumValueMapper.insert(valueDO);
            }
        }

        // 重新加载完整枚举
        MetadataEnum saved = getEnumById(dbEntity.getId());
        return Result.ok(saved);
    }

    @Override
    @CacheEvict(value = CACHE_ENUMS, key = CACHE_KEY_ALL)
    @Transactional  // C1: 添加事务保护
    public boolean deleteEnum(Long id) {
        // 1. 查询引用
        List<MetadataFieldDO> refFields = metadataFieldMapper.selectByEnumId(id);
        if (refFields != null && !refFields.isEmpty()) {
            return false;
        }

        // 2. 删除枚举值
        metadataEnumValueMapper.deleteByEnumId(id);

        // 3. 软删除枚举（将 status 设为 DELETED）
        MetadataEnumDO enumDO = metadataEnumMapper.selectById(id);
        if (enumDO != null) {
            enumDO.setStatus(STATUS_DELETED);
            metadataEnumMapper.updateById(enumDO);
        }

        return true;
    }

    // ===== 枚举值管理 =====

    @Override
    public List<MetadataEnumValue> getEnumValuesByEnumId(Long enumId) {
        List<MetadataEnumValueDO> valueDOs = metadataEnumValueMapper.selectByEnumId(enumId);
        return valueDOs.stream().map(DomainConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public Result<MetadataEnumValue> saveEnumValue(MetadataEnumValue value) {
        MetadataEnumValueDO dbEntity = DbConverter.toDbEntity(value);

        if (dbEntity.getId() == null || dbEntity.getId() == 0L) {
            metadataEnumValueMapper.insert(dbEntity);
            value.setId(dbEntity.getId());
        } else {
            metadataEnumValueMapper.updateById(dbEntity);
        }

        return Result.ok(value);
    }

    @Override
    public void deleteEnumValue(Long id) {
        metadataEnumValueMapper.deleteById(id);
    }

    // ===== Schema 校验 =====

    @Override
    public ValidationResult validateSchema(Long modelId) {
        ValidationResult result = new ValidationResult();

        // 1. 加载模型
        MetadataModel model = getModelById(modelId);
        if (model == null) {
            result.addError("model", "模型不存在: id=" + modelId);
            return result;
        }

        // 2. 加载字段
        List<MetadataField> fields = getFieldsByModelId(modelId);

        // 3. 校验每个字段
        for (MetadataField field : fields) {
            String fieldKey = field.getFieldName() != null ? field.getFieldName() : "id=" + field.getId();

            // fieldName 不为空
            if (field.getFieldName() == null || field.getFieldName().trim().isEmpty()) {
                result.addError(fieldKey, "fieldName 不能为空");
            }

            // fieldType 在合法列表中
            if (field.getFieldType() == null || !VALID_FIELD_TYPES.contains(field.getFieldType())) {
                result.addError(fieldKey, "fieldType 不合法: " + field.getFieldType()
                        + "，合法值为: " + VALID_FIELD_TYPES);
            }

            // ENUM 类型必须有 enumId
            if ("ENUM".equals(field.getFieldType()) && field.getEnumId() == null) {
                result.addError(fieldKey, "fieldType=ENUM 时必须指定 enumId");
            }

            // enumId 对应的枚举存在且未被删除
            if (field.getEnumId() != null) {
                MetadataEnumDO enumDO = metadataEnumMapper.selectById(field.getEnumId());
                if (enumDO == null) {
                    result.addError(fieldKey, "enumId=" + field.getEnumId() + " 对应的枚举不存在");
                } else if (STATUS_DELETED.equals(enumDO.getStatus())) {
                    result.addError(fieldKey, "enumId=" + field.getEnumId() + " 对应的枚举("
                            + enumDO.getName() + ")已被删除");
                }
            }

            // required=true 时 businessMeaning 不能为空
            if (Boolean.TRUE.equals(field.getRequired())
                    && (field.getBusinessMeaning() == null || field.getBusinessMeaning().trim().isEmpty())) {
                result.addError(fieldKey, "required=true 时 businessMeaning 不能为空");
            }
        }

        // 4. C4: 仅在校验通过时更新模型状态，并手动清缓存（避免 @CacheEvict 在失败时也清缓存）
        if (result.isValid()) {
            MetadataModelDO modelDO = metadataModelMapper.selectById(modelId);
            if (modelDO != null) {
                modelDO.setStatus(STATUS_PUBLISHED);
                metadataModelMapper.updateById(modelDO);
            }
            // 校验通过后手动清模型缓存
            org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_MODELS);
            if (cache != null) {
                cache.clear();
            }
        }

        return result;
    }

    // ===== 模型发布 =====

    @Override
    @CacheEvict(value = CACHE_MODELS, key = CACHE_KEY_ALL)
    public void publishModel(Long modelId) {
        MetadataModelDO modelDO = metadataModelMapper.selectById(modelId);
        if (modelDO == null) {
            return;
        }
        modelDO.setStatus(STATUS_PUBLISHED);
        metadataModelMapper.updateById(modelDO);
    }

    // ===== JSON Schema 生成 =====

    @Override
    public Map<String, Object> generateJsonSchema(Long modelId) {
        Map<String, Object> schema = new LinkedHashMap<>();

        MetadataModelDO modelDO = metadataModelMapper.selectById(modelId);
        if (modelDO == null) {
            schema.put("error", "模型不存在: id=" + modelId);
            return schema;
        }

        List<MetadataFieldDO> fieldDOs = metadataFieldMapper.selectByModelId(modelId);

        schema.put("$schema", "https://json-schema.org/draft-07/schema#");
        schema.put("type", "object");
        schema.put("title", modelDO.getCode() + " - " + modelDO.getName());
        schema.put("description", modelDO.getDescription() != null ? modelDO.getDescription() : "");

        // properties
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (MetadataFieldDO field : fieldDOs) {
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("type", toJsonSchemaType(field.getFieldType()));
            if (field.getBusinessMeaning() != null && !field.getBusinessMeaning().isEmpty()) {
                prop.put("description", field.getBusinessMeaning());
            }

            // ENUM 类型: 填充 enum 值列表
            if ("ENUM".equals(field.getFieldType()) && field.getEnumId() != null) {
                List<MetadataEnumValueDO> values = metadataEnumValueMapper.selectByEnumId(field.getEnumId());
                if (values != null && !values.isEmpty()) {
                    List<String> enumValues = values.stream()
                            .map(MetadataEnumValueDO::getValueCode)
                            .collect(Collectors.toList());
                    prop.put("enum", enumValues);
                }
            }

            // INTEGER 类型默认值
            if ("INTEGER".equals(field.getFieldType()) || "NUMBER".equals(field.getFieldType())) {
                prop.put("default", 0);
            } else if ("BOOLEAN".equals(field.getFieldType())) {
                prop.put("default", false);
            } else if ("STRING".equals(field.getFieldType())) {
                prop.put("default", "");
            }

            properties.put(field.getFieldName(), prop);

            if (field.getRequired() != null && field.getRequired() == 1) {
                required.add(field.getFieldName());
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    /**
     * 将内部字段类型映射为 JSON Schema type。
     */
    private String toJsonSchemaType(String fieldType) {
        if (fieldType == null) {
            return "string";
        }
        switch (fieldType) {
            case "STRING":
            case "DATE":
            case "DATETIME":
            case "ENUM":
                return "string";
            case "INTEGER":
                return "integer";
            case "NUMBER":
            case "DECIMAL":
                return "number";
            case "BOOLEAN":
                return "boolean";
            default:
                return "string";
        }
    }

    // ===== 枚举使用统计 =====

    @Override
    public EnumUsage getEnumUsage(Long enumId) {
        MetadataEnumDO enumDO = metadataEnumMapper.selectById(enumId);
        if (enumDO == null) {
            return null;
        }

        EnumUsage usage = new EnumUsage();
        usage.setEnumId(enumId);
        usage.setEnumName(enumDO.getName());

        // 查询引用该枚举的所有字段
        List<MetadataFieldDO> refFields = metadataFieldMapper.selectByEnumId(enumId);
        if (refFields == null || refFields.isEmpty()) {
            usage.setTotalRefCount(0);
            usage.setRefModelCount(0);
            usage.setRefFieldCount(0);
            usage.setRefDetails(Collections.emptyList());
            return usage;
        }

        usage.setTotalRefCount(refFields.size());
        usage.setRefFieldCount(refFields.size());

        // 按 modelId 分组统计
        Map<Long, List<MetadataFieldDO>> groupedByModel = refFields.stream()
                .collect(Collectors.groupingBy(MetadataFieldDO::getModelId));

        usage.setRefModelCount(groupedByModel.size());

        // 构建 refDetails
        List<EnumUsage.RefDetail> refDetails = new ArrayList<>();
        for (Map.Entry<Long, List<MetadataFieldDO>> entry : groupedByModel.entrySet()) {
            Long modelId = entry.getKey();
            List<MetadataFieldDO> modelFields = entry.getValue();

            EnumUsage.RefDetail detail = new EnumUsage.RefDetail();
            detail.setModelId(modelId);

            MetadataModelDO modelDO = metadataModelMapper.selectById(modelId);
            if (modelDO != null) {
                detail.setModelName(modelDO.getName());
                detail.setModelType(modelDO.getModelType());
            }

            List<EnumUsage.RefField> fieldList = modelFields.stream()
                    .map(f -> new EnumUsage.RefField(f.getId(), f.getFieldName()))
                    .collect(Collectors.toList());
            detail.setFields(fieldList);

            refDetails.add(detail);
        }
        usage.setRefDetails(refDetails);

        return usage;
    }
}
