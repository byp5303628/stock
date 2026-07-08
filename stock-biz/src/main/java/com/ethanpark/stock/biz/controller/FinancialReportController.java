package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.converter.DtoConverter;
import com.ethanpark.stock.biz.dto.FinancialReportDTO;
import com.ethanpark.stock.biz.dto.FinancialReportSaveRequest;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.exception.BusinessException;
import com.ethanpark.stock.core.model.FinancialReport;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.FinancialReportDomainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务报表 Controller。
 *
 * <p>财报数据由爬虫入库，本模块提供查询能力。
 * CLI 友好的精确查询通过业务键（股票代码 + 报表类型 + 报告日期）定位。
 *
 * @author baiyunpeng04
 * @since 2025/07/02
 */
@RestController
@RequestMapping("/api/financial-reports")
public class FinancialReportController {

    @Resource
    private FinancialReportDomainService financialReportDomainService;

    /**
     * 查询财报列表。
     *
     * <p>当提供 code + reportType + reportDate 时，返回精确匹配的单条结果。
     * 否则按条件分页查询。
     *
     * @param code        股票代码
     * @param reportType  报表类型（CASH_FLOW/BALANCE/INCOME）
     * @param reportPeriod 报告期（Q1/Q2/Q3/Q4/YEARLY）
     * @param reportDate  报告截止日期（yyyy-MM-dd）
     * @param fiscalYear  会计年度
     * @param page        页码
     * @param size        每页大小
     * @return 财报列表或分页结果
     */
    @GetMapping
    public ResponseDTO<?> query(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String reportPeriod,
            @RequestParam(required = false) String reportDate,
            @RequestParam(required = false) Integer fiscalYear,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        // 精确查询：code + reportType + reportDate
        if (code != null && reportType != null && reportDate != null) {
            FinancialReport report = financialReportDomainService.getByUniqueKey(code, reportType, reportDate);
            if (report == null) {
                throw new BusinessException(ErrorCode.ILLEGAL_PARAM.getCode(),
                        "财报不存在: code=" + code + ", type=" + reportType + ", date=" + reportDate);
            }
            return ResponseDTO.success(DtoConverter.toDto(report));
        }

        // 分页查询
        PageResult<FinancialReport> pageResult = financialReportDomainService.query(
                code, reportType, reportPeriod, null, null, fiscalYear, page, size);

        PageResult<FinancialReportDTO> dtoResult = new PageResult<>();
        dtoResult.setTotal(pageResult.getTotal());
        dtoResult.setPage(pageResult.getPage());
        dtoResult.setSize(pageResult.getSize());
        dtoResult.setPages(pageResult.getPages());
        dtoResult.setItems(pageResult.getItems().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList()));

        return ResponseDTO.success(dtoResult);
    }

    /**
     * 查询指定股票的最新各类型财报。
     *
     * <p>按报表类型分组，取每种类型最新的一份。
     *
     * @param code 股票代码
     * @return 最新财报列表
     */
    @GetMapping("/latest")
    public ResponseDTO<List<FinancialReportDTO>> latest(@RequestParam String code) {
        List<FinancialReport> reports = financialReportDomainService.getLatestByCode(code);
        List<FinancialReportDTO> dtos = reports.stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
        return ResponseDTO.success(dtos);
    }

    /**
     * 创建或更新财务报表。
     *
     * <p>使用 upsert 语义：存在相同唯一键的记录则更新，否则插入。
     * 本接口主要供爬虫调用来入库数据。
     *
     * @param request 财报保存请求
     * @return 保存后的完整财报
     */
    @PostMapping
    public ResponseDTO<FinancialReportDTO> save(@RequestBody @Valid FinancialReportSaveRequest request) {
        FinancialReport report = DtoConverter.toDomain(request);
        FinancialReport saved = financialReportDomainService.save(report);
        return ResponseDTO.success(DtoConverter.toDto(saved));
    }
}
