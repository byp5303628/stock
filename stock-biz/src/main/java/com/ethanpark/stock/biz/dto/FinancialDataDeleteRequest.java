package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 金融数据删除请求。
 *
 * <p>通过 (market + code + modelCode + partitionDate) 唯一键定位要删除的记录。
 *
 * @author baiyunpeng04
 * @since 2025/07/10
 */
@Getter
@Setter
public class FinancialDataDeleteRequest {

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
}
