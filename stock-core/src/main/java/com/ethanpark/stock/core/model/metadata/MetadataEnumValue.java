package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * 元数据枚举值领域对象，对应 metadata_enum_value 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataEnumValue {
    private Long id;
    private Long enumId;
    private String valueCode;
    private String valueLabel;
    private Integer sortOrder;
    private Map<String, Object> extInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
