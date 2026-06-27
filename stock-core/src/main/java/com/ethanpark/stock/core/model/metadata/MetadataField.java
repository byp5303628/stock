package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * 元数据字段领域对象，对应 metadata_field 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataField {
    private Long id;
    private Long modelId;
    private String fieldName;
    private String fieldType;
    private String businessMeaning;
    private Boolean required;
    private Map<String, Object> constraints;
    private Long enumId;
    private Integer sortOrder;
    private Map<String, Object> extInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
