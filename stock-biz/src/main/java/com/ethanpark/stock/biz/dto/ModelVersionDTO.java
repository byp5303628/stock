package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 模型版本 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class ModelVersionDTO {
    private Long id;
    private Integer version;
    private String versionDesc;
    private Boolean isCurrent; // whether this is the current_version
    private String gmtCreate; // formatted as yyyy-MM-dd HH:mm:ss
}