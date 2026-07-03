package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2025/07/02
 */
@Getter
@Setter
public class FinancialReportSaveRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String reportType;

    @NotBlank
    private String reportDate;

    private String reportPeriod;

    private Map<String, Object> reportData;

    private String currency;

    private String unit;

    private String source;
}
