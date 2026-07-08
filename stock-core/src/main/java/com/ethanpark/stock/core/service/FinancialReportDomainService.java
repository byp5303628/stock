package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.FinancialReport;
import com.ethanpark.stock.core.model.PageResult;

import java.util.List;

/**
 * 财报领域服务接口。
 *
 * <p>提供财务报表（资产负债表、利润表、现金流量表）的 CRUD 操作，
 * 支持按股票代码、报表类型、报告期、会计年度等维度查询。
 *
 * @author baiyunpeng04
 * @since 2025/07/02
 */
public interface FinancialReportDomainService {

    /**
     * 保存一份财报。
     *
     * <p>使用 upsert 语义：存在相同唯一键的记录则更新，否则插入。
     *
     * @param report 财报领域对象
     * @return 保存后的完整财报（含数据库生成 ID）
     */
    FinancialReport save(FinancialReport report);

    /**
     * 分页查询财报。
     *
     * @param code        股票代码（可选）
     * @param reportType  报表类型（可选，CASH_FLOW/BALANCE/INCOME）
     * @param reportPeriod 报告期（可选，Q1/Q2/Q3/Q4/YEARLY）
     * @param startDate   起始报告日期（可选，yyyy-MM-dd）
     * @param endDate     截止报告日期（可选，yyyy-MM-dd）
     * @param fiscalYear  会计年度（可选）
     * @param page        页码，从 1 开始
     * @param size        每页大小
     * @return 分页结果
     */
    PageResult<FinancialReport> query(String code, String reportType, String reportPeriod,
                                      String startDate, String endDate,
                                      Integer fiscalYear, int page, int size);

    /**
     * 按业务键精确查询财报。
     *
     * <p>code + reportType + reportDate 为唯一键。
     *
     * @param code       股票代码
     * @param reportType 报表类型
     * @param reportDate 报告截止日期（yyyy-MM-dd）
     * @return 财报领域对象，不存在返回 null
     */
    FinancialReport getByUniqueKey(String code, String reportType, String reportDate);

    /**
     * 查询指定股票的最新各类型财报。
     *
     * <p>按报表类型分组，取每种类型最新的一份。
     *
     * @param code 股票代码
     * @return 最新财报列表
     */
    List<FinancialReport> getLatestByCode(String code);

    /**
     * 删除指定 ID 的财报。
     *
     * @param id 财报 ID
     */
    void deleteById(Long id);
}
