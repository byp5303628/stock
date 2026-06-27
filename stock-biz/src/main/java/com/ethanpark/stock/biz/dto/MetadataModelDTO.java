package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 元数据模型 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataModelDTO {
    private Long id;
    private String name;
    private String code;
    private String modelType;
    private String description;
    private String status;
    private List<MetadataFieldDTO> fields;
    private String gmtCreate;
    private String gmtModified;
}
