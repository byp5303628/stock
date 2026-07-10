package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 金融数据查询请求。
 *
 * <p>当提供 modelCode + partitionDate 时为精确查询，返回单条记录；
 * 否则为分页查询，支持按时间范围过滤。
 *
 * @author baiyunpeng04
 * @since 2025/07/10
 */
@Getter
@Setter
public class FinancialDataQueryRequest {

    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    /** 市场代码，默认 SH */
    private String market = "SH";

    @NotBlank(message = "code 不能为空")
    private String code;

    /** 模型编码，精确查询时必传 */
    private String modelCode;

    /** 分区日期，精确查询时必传，格式 yyyy-MM-dd */
    private String partitionDate;

    /** 起始日期，分页查询时可选 */
    private String startDate;

    /** 截止日期，分页查询时可选 */
    private String endDate;

    /** 页码，从 1 开始 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 20;
}
