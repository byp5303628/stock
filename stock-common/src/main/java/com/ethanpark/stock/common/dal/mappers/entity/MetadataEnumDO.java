package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 元数据枚举 DO，对应 metadata_enum 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnumDO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private Date gmtCreate;
    private Date gmtModified;
}
