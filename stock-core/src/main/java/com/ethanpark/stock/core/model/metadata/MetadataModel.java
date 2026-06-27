package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 元数据模型领域对象，对应 metadata_model 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataModel {
    private Long id;
    private String name;
    private String code;
    private String modelType;
    private String description;
    private String status;
    private Integer currentVersion;
    private String snapshotHash;
    private Map<String, Object> extInfo;
    private List<MetadataField> fields;
    private Date gmtCreate;
    private Date gmtModified;
}
