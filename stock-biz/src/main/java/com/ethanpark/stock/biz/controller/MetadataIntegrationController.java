package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.exception.BusinessException;
import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.MetadataDomainService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 元数据集成 API Controller。
 *
 * <p>提供面向业务方的指标语义查询和取数规则查询能力。
 *
 * <p>类级别路径: /api/metadata/indicator
 * 方法级别路径不再重复 "indicator" 前缀（C2: 修复路径重复问题）。
 *
 * @author baiyunpeng04
 */
@RestController
@RequestMapping("/api/metadata/indicator")
public class MetadataIntegrationController {

    private final MetadataDomainService metadataDomainService;

    public MetadataIntegrationController(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }

    /**
     * 根据模型编码查询指标字段语义描述。
     *
     * <p>实际路径: GET /api/metadata/indicator/meaning
     */
    @GetMapping("/meaning")
    public ResponseDTO<Map<String, Object>> getIndicatorMeaning(@RequestParam String code) {
        MetadataModel model = metadataDomainService.getModelByCode(code);
        if (model == null) {
            throw new BusinessException(ErrorCode.METADATA_MODEL_NOT_FOUND);
        }

        List<MetadataField> fields = metadataDomainService.getFieldsByModelId(model.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("modelName", model.getName());
        result.put("modelCode", model.getCode());
        result.put("modelType", model.getModelType());
        result.put("description", model.getDescription());

        List<Map<String, Object>> fieldDescs = fields.stream().map(f -> {
            Map<String, Object> fieldDesc = new HashMap<>();
            fieldDesc.put("fieldName", f.getFieldName());
            fieldDesc.put("fieldType", f.getFieldType());
            fieldDesc.put("businessMeaning", f.getBusinessMeaning());
            fieldDesc.put("required", f.getRequired());
            return fieldDesc;
        }).collect(Collectors.toList());

        result.put("fields", fieldDescs);

        return ResponseDTO.success(result);
    }

    /**
     * 根据模型编码查询指标取数规则和使用说明。
     *
     * <p>实际路径: GET /api/metadata/indicator/usage
     */
    @GetMapping("/usage")
    public ResponseDTO<Map<String, Object>> getIndicatorUsage(@RequestParam String code) {
        MetadataModel model = metadataDomainService.getModelByCode(code);
        if (model == null) {
            throw new BusinessException(ErrorCode.METADATA_MODEL_NOT_FOUND);
        }

        List<MetadataField> fields = metadataDomainService.getFieldsByModelId(model.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("modelName", model.getName());
        result.put("modelCode", model.getCode());
        result.put("modelType", model.getModelType());
        result.put("status", model.getStatus());

        List<Map<String, Object>> fieldRules = fields.stream().map(f -> {
            Map<String, Object> fieldRule = new HashMap<>();
            fieldRule.put("fieldName", f.getFieldName());
            fieldRule.put("fieldType", f.getFieldType());
            fieldRule.put("constraints", f.getConstraints());
            fieldRule.put("extInfo", f.getExtInfo());
            return fieldRule;
        }).collect(Collectors.toList());

        result.put("fields", fieldRules);

        return ResponseDTO.success(result);
    }
}
