package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Data
public class FinancialReportDO {
    private Long id;

    private String code;

    private String reportType;

    private String reportDate;

    private String reportPeriod;

    /**
     * JSON text — indicator data, key = metadata field name
     */
    private String reportData;

    private Integer fiscalYear;

    private String currency;

    private String unit;

    private String source;

    private Date gmtCreate;

    private Date gmtModified;
}
