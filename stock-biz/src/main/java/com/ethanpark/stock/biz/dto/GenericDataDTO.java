package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class GenericDataDTO {
    private Long id;
    private String market;
    private String code;
    private String modelCode;
    private String partitionDate;
    private Map<String, Object> dataContent;
    private Map<String, Object> extraInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
