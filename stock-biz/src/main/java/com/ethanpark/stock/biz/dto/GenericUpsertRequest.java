package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Getter
@Setter
public class GenericUpsertRequest {
    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    @NotBlank(message = "market 不能为空")
    private String market;

    @NotBlank(message = "code 不能为空")
    private String code;

    @NotBlank(message = "modelCode 不能为空")
    private String modelCode;

    @NotBlank(message = "partitionDate 不能为空")
    private String partitionDate;

    private Map<String, Object> dataContent;
    private Map<String, Object> extraInfo;
}
