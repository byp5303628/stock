package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2025/07/02
 */
@Getter
@Setter
public class FinancialReport {
    private Long id;

    private String code;

    private String reportType;

    private String reportDate;

    private String reportPeriod;

    private Map<String, Object> reportData;

    private Integer fiscalYear;

    private String currency;

    private String unit;

    private String source;

    private Date gmtCreate;

    private Date gmtModified;
}
