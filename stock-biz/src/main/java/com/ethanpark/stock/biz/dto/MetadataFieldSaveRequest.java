package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 元数据字段保存请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataFieldSaveRequest {
    private Long id;

    @NotNull(message = "字段所属模型 ID 不能为空")
    private Long modelId;

    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    private String businessMeaning;
    private Boolean required;
    private String constraints;
    private Long enumId;
    private Integer sortOrder;
}
