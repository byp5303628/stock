package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 元数据模型版本 DO，对应 metadata_model_version 表。
 * 存储元数据模型每次修改的快照，支持版本管理和变更追溯。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataModelVersionDO {
    private Long id;
    private String modelCode;
    private Long modelId;
    private Integer version;
    private String schemaContent;
    private String versionDesc;
    private Date gmtCreate;
    private Date gmtModified;
}
