package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2025/07/02
 */
@Getter
@Setter
public class FinancialReportQueryRequest {
    private String code;

    private String reportType;

    private String startDate;

    private String endDate;

    private Integer fiscalYear;

    private Integer page = 1;

    private Integer size = 20;
}
