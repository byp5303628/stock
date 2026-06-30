package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 元数据模型 DO，对应 metadata_model 表。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class MetadataModelDO {
    private Long id;
    private String name;
    private String code;
    private String modelType;
    private String description;
    private String status;
    private String extInfo;
    private Integer currentVersion;
    private String snapshotHash;
    private Date gmtCreate;
    private Date gmtModified;
}
