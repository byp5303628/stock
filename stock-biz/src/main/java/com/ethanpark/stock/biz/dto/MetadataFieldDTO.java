package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 元数据字段 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataFieldDTO {
    private Long id;
    private Long modelId;
    private String fieldName;
    private String fieldType;
    private String businessMeaning;
    private Boolean required;
    private String constraints;
    private Long enumId;
    private Integer sortOrder;
    private String extInfo;
}
