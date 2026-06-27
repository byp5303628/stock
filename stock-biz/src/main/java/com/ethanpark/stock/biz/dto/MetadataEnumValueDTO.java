package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 元数据枚举值 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnumValueDTO {
    private Long id;
    private Long enumId;
    private String valueCode;
    private String valueLabel;
    private Integer sortOrder;
}
