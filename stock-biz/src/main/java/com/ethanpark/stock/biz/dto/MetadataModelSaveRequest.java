package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 元数据模型保存请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataModelSaveRequest {
    private Long id;

    @NotBlank(message = "模型名称不能为空")
    private String name;

    @NotBlank(message = "模型编码不能为空")
    private String code;

    @NotBlank(message = "模型类型不能为空")
    private String modelType;

    private String description;
}
