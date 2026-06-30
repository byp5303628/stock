package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * 元数据模型版本领域对象，对应 metadata_model_version 表。
 *
 * <p>每次发布生成一个不可变的版本快照，包含缓存的 JSONSchema 内容，
 * 支持版本列表查看和版本切换功能。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataModelVersion {
    private Long id;
    private String modelCode;
    private Long modelId;
    private Integer version;
    private Map<String, Object> schemaContent;
    private String versionDesc;
    private Date gmtCreate;
    private Date gmtModified;
}
