package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 元数据字段 DO，对应 metadata_field 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataFieldDO {
    private Long id;
    private Long modelId;
    private String fieldName;
    private String fieldType;
    private String businessMeaning;
    private Integer required;
    private String constraints;
    private Long enumId;
    private Integer sortOrder;
    private String extInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
