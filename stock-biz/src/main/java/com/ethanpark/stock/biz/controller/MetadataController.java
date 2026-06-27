package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.converter.DtoConverter;
import com.ethanpark.stock.biz.dto.*;
import com.ethanpark.stock.biz.exception.BusinessException;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.metadata.*;
import com.ethanpark.stock.core.service.MetadataDomainService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 元数据管理 Controller。
 *
 * <p>提供元数据模型、字段、枚举的 REST API，遵循 Curl 友好的设计规范。
 *
 * @author baiyunpeng04
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataDomainService metadataDomainService;

    public MetadataController(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }

    // ===== 模型管理 =====

    /**
     * 获取所有元数据模型列表。
     */
    @GetMapping("/model/list.json")
    public ResponseDTO<List<MetadataModelDTO>> listModels() {
        List<MetadataModel> models = metadataDomainService.listModels();
        List<MetadataModelDTO> dtos = models.stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
        return ResponseDTO.success(dtos);
    }

    /**
     * 获取单个模型详情（含字段列表）。
     */
    @GetMapping("/model/detail.json")
    public ResponseDTO<MetadataModelDTO> getModelDetail(@RequestParam Long id) {
        MetadataModel model = metadataDomainService.getModelById(id);
        if (model == null) {
            throw new BusinessException(ErrorCode.METADATA_MODEL_NOT_FOUND);
        }

        // 加载字段列表
        List<MetadataField> fields = metadataDomainService.getFieldsByModelId(id);
        model.setFields(fields);

        MetadataModelDTO dto = DtoConverter.toDto(model);
        dto.setCurrentVersion(model.getCurrentVersion());
        return ResponseDTO.success(dto);
    }

    /**
     * 新建/更新元数据模型。
     */
    @PostMapping("/model/save.json")
    public ResponseDTO<MetadataModelDTO> saveModel(@RequestBody @Valid MetadataModelSaveRequest request) {
        MetadataModel model = DtoConverter.toDomain(request);
        Result<MetadataModel> result = metadataDomainService.saveModel(model);

        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }

        return ResponseDTO.success(DtoConverter.toDto(result.getData()));
    }

    /**
     * Schema 校验。
     */
    @PostMapping("/model/validate.json")
    public ResponseDTO<ValidationResultDTO> validateModel(@RequestBody @Valid ValidateRequest request) {
        ValidationResult result = metadataDomainService.validateSchema(request.getModelId());
        return ResponseDTO.success(DtoConverter.toDto(result));
    }

    /**
     * 发布模型（生成版本快照）。
     */
    @PostMapping("/model/publish.json")
    public ResponseDTO<Void> publishModel(@RequestBody @Valid PublishModelRequest request) {
        metadataDomainService.publishModel(request.getModelId(), request.getVersionDesc());
        return ResponseDTO.success();
    }

    /**
     * 生成模型的 JSON Schema。
     *
     * <p>根据模型字段配置动态生成 JSON Schema (draft-07)，可用于 AI 或系统间数据校验。
     * 支持指定版本号获取对应版本的 Schema。
     */
    @GetMapping("/model/schema.json")
    public ResponseDTO<Map<String, Object>> getModelSchema(
            @RequestParam Long id,
            @RequestParam(required = false) Integer version) {
        Map<String, Object> schema;
        if (version != null) {
            schema = metadataDomainService.getSchemaByVersion(id, version);
        } else {
            schema = metadataDomainService.getModelSchema(id);
        }
        return ResponseDTO.success(schema);
    }

    /**
     * 删除元数据模型（物理删除，同时删除关联字段）。
     */
    @DeleteMapping("/model/delete.json")
    public ResponseDTO<Void> deleteModel(@RequestParam Long id) {
        metadataDomainService.deleteModel(id);
        return ResponseDTO.success();
    }

    // ===== 版本管理 =====

    /**
     * 获取模型版本列表。
     *
     * <p>返回模型的所有历史版本，标记当前生效版本。
     */
    @GetMapping("/model/versions.json")
    public ResponseDTO<List<ModelVersionDTO>> listModelVersions(@RequestParam Long id) {
        MetadataModel model = metadataDomainService.getModelById(id);
        if (model == null) {
            throw new BusinessException(ErrorCode.METADATA_MODEL_NOT_FOUND);
        }
        List<MetadataModelVersion> versions = metadataDomainService.listModelVersions(id);
        Integer currentVersion = model.getCurrentVersion() == null ? 0 : model.getCurrentVersion();
        // list is desc sorted by version
        int maxVersion = versions.isEmpty() ? 0 : versions.get(0).getVersion();
        List<ModelVersionDTO> dtos = new ArrayList<>();
        for (MetadataModelVersion v : versions) {
            ModelVersionDTO dto = DtoConverter.toDto(v);
            boolean isCurrent = (currentVersion == 0)
                    ? v.getVersion().equals(maxVersion)
                    : v.getVersion().equals(currentVersion);
            dto.setIsCurrent(isCurrent);
            dtos.add(dto);
        }
        return ResponseDTO.success(dtos);
    }

    /**
     * 切换当前生效版本。
     */
    @PostMapping("/model/switch-version.json")
    public ResponseDTO<Void> switchModelVersion(@RequestBody @Valid SwitchVersionRequest request) {
        metadataDomainService.switchModelVersion(request.getModelId(), request.getVersion());
        return ResponseDTO.success();
    }

    // ===== 字段管理 =====

    /**
     * 新建/更新字段。
     */
    @PostMapping("/field/save.json")
    public ResponseDTO<MetadataFieldDTO> saveField(@RequestBody @Valid MetadataFieldSaveRequest request) {
        MetadataField field = DtoConverter.toDomain(request);
        Result<MetadataField> result = metadataDomainService.saveField(field);

        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }

        return ResponseDTO.success(DtoConverter.toDto(result.getData()));
    }

    /**
     * 删除字段。
     */
    @DeleteMapping("/field/delete.json")
    public ResponseDTO<Void> deleteField(@RequestParam Long id) {
        metadataDomainService.deleteField(id);
        return ResponseDTO.success();
    }

    // ===== 枚举管理 =====

    /**
     * 获取所有枚举列表（含各枚举引用数）。
     */
    @GetMapping("/enum/list.json")
    public ResponseDTO<List<MetadataEnumDTO>> listEnums() {
        List<MetadataEnum> enums = metadataDomainService.listEnumsWithUsage();
        List<MetadataEnumDTO> dtos = enums.stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
        return ResponseDTO.success(dtos);
    }

    /**
     * 获取单个枚举详情（含枚举值 + 引用列表）。
     */
    @GetMapping("/enum/detail.json")
    public ResponseDTO<MetadataEnumDTO> getEnumDetail(@RequestParam Long id) {
        MetadataEnum metadataEnum = metadataDomainService.getEnumById(id);
        if (metadataEnum == null) {
            throw new BusinessException(ErrorCode.METADATA_ENUM_NOT_FOUND);
        }

        MetadataEnumDTO dto = DtoConverter.toDto(metadataEnum);

        // 填充引用信息
        EnumUsage usage = metadataDomainService.getEnumUsage(id);
        if (usage != null) {
            dto.setRefModelCount(usage.getRefModelCount());
            dto.setRefFieldCount(usage.getRefFieldCount());
            dto.setReferencedBy(DtoConverter.toDto(usage).getRefDetails());
        }

        return ResponseDTO.success(dto);
    }

    /**
     * 新建/更新枚举（含枚举值）。
     */
    @PostMapping("/enum/save.json")
    public ResponseDTO<MetadataEnumDTO> saveEnum(@RequestBody @Valid MetadataEnumSaveRequest request) {
        MetadataEnum metadataEnum = DtoConverter.toDomain(request);
        Result<MetadataEnum> result = metadataDomainService.saveEnum(metadataEnum);

        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }

        return ResponseDTO.success(DtoConverter.toDto(result.getData()));
    }

    /**
     * C5: 真正的枚举删除接口，有引用时返回错误。
     *
     * <p>前端 EnumManager 应调用此接口而非 saveEnum 伪装删除。
     */
    @DeleteMapping("/enum/delete.json")
    public ResponseDTO<Void> deleteEnum(@RequestParam Long id) {
        boolean deleted = metadataDomainService.deleteEnum(id);
        if (!deleted) {
            throw new BusinessException(ErrorCode.METADATA_ENUM_HAS_REFS);
        }
        return ResponseDTO.success();
    }

    /**
     * 绑定枚举到字段。
     *
     * <p>先加载完整字段对象，再修改 enumId 后全量 save，避免 updateById 将所有未设置字段覆盖为 null。
     */
    @PostMapping("/enum/bind.json")
    public ResponseDTO<Void> bindEnum(@RequestBody @Valid EnumBindRequest request) {
        // 确认枚举存在
        MetadataEnum metadataEnum = metadataDomainService.getEnumById(request.getEnumId());
        if (metadataEnum == null) {
            throw new BusinessException(ErrorCode.METADATA_ENUM_NOT_FOUND);
        }

        // 加载完整字段对象后仅修改 enumId
        MetadataField fullField = metadataDomainService.getFieldById(request.getFieldId());
        if (fullField == null) {
            throw new BusinessException(ErrorCode.METADATA_FIELD_NOT_FOUND);
        }

        fullField.setEnumId(request.getEnumId());
        Result<MetadataField> result = metadataDomainService.saveField(fullField);
        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }

        return ResponseDTO.success();
    }

    /**
     * 解除枚举与字段的绑定。
     *
     * <p>先加载完整字段对象，再将 enumId 置为 null 后全量 save，避免 updateById 将所有未设置字段覆盖为 null。
     */
    @PostMapping("/enum/unbind.json")
    public ResponseDTO<Void> unbindEnum(@RequestBody @Valid EnumUnbindRequest request) {
        MetadataField fullField = metadataDomainService.getFieldById(request.getFieldId());
        if (fullField == null) {
            throw new BusinessException(ErrorCode.METADATA_FIELD_NOT_FOUND);
        }

        fullField.setEnumId(null);
        Result<MetadataField> result = metadataDomainService.saveField(fullField);
        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }

        return ResponseDTO.success();
    }

    /**
     * 查询枚举使用情况。
     */
    @GetMapping("/enum/usage.json")
    public ResponseDTO<EnumUsageDTO> getEnumUsage(@RequestParam Long id) {
        EnumUsage usage = metadataDomainService.getEnumUsage(id);
        if (usage == null) {
            throw new BusinessException(ErrorCode.METADATA_ENUM_NOT_FOUND);
        }
        return ResponseDTO.success(DtoConverter.toDto(usage));
    }
}