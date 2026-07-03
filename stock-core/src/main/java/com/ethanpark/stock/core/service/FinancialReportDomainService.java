package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.FinancialReport;
import com.ethanpark.stock.core.model.PageResult;

import java.util.List;

/**
 * 财报领域服务接口。
 *
 * <p>提供财务报表（资产负债表、利润表、现金流量表）的完整 CRUD 操作，
 * 支持按股票代码、报表类型、会计年度等维度查询。
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
     * 批量保存多份财报。
     *
     * @param reports 财报列表
     * @return 保存后的完整财报列表
     */
    List<FinancialReport> batchSave(List<FinancialReport> reports);

    /**
     * 分页查询财报。
     *
     * @param code       股票代码（可选）
     * @param reportType 报表类型（可选，CASH_FLOW/BALANCE/INCOME）
     * @param startDate  起始报告日期（可选，yyyy-MM-dd）
     * @param endDate    截止报告日期（可选，yyyy-MM-dd）
     * @param fiscalYear 会计年度（可选）
     * @param page       页码，从 1 开始
     * @param size       每页大小
     * @return 分页结果
     */
    PageResult<FinancialReport> query(String code, String reportType,
                                      String startDate, String endDate,
                                      Integer fiscalYear, int page, int size);

    /**
     * 根据 ID 查询财报。
     *
     * @param id 财报 ID
     * @return 财报领域对象，不存在返回 null
     */
    FinancialReport getById(Long id);

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
     * 根据股票代码和报表类型查询。
     *
     * <p>按报告日期降序排列。
     *
     * @param code       股票代码
     * @param reportType 报表类型
     * @return 财报列表
     */
    List<FinancialReport> getByCodeAndType(String code, String reportType);

    /**
     * 删除指定 ID 的财报。
     *
     * @param id 财报 ID
     */
    void deleteById(Long id);
}
