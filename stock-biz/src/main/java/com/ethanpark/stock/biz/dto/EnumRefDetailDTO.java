package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 枚举引用详情 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class EnumRefDetailDTO {
    private Long modelId;
    private String modelName;
    private String modelType;
    private List<RefFieldDTO> fields;
}
