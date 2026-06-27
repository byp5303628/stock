package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 元数据枚举 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnumDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private List<MetadataEnumValueDTO> values;
    private int refModelCount;
    private int refFieldCount;
    private List<EnumRefDetailDTO> referencedBy;
}
