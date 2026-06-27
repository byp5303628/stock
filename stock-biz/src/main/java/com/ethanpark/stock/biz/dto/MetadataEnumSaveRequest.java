package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 元数据枚举保存请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnumSaveRequest {
    private Long id;

    @NotBlank(message = "枚举名称不能为空")
    private String name;

    @NotBlank(message = "枚举编码不能为空")
    private String code;

    private String description;
    private List<MetadataEnumValueDTO> values;
}
