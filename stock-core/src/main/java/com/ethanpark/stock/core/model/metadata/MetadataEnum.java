package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 元数据枚举领域对象，对应 metadata_enum 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnum {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private List<MetadataEnumValue> values;
    private Integer refModelCount;
    private Integer refFieldCount;
    private Date gmtCreate;
    private Date gmtModified;
}
