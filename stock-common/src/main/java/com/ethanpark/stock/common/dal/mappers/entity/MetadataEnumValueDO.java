package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 元数据枚举值 DO，对应 metadata_enum_value 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnumValueDO {
    private Long id;
    private Long enumId;
    private String valueCode;
    private String valueLabel;
    private Integer sortOrder;
    private String extInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
