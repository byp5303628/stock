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

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务报表 Controller。
 *
 * <p>提供财务报表（资产负债表、利润表、现金流量表）的 REST API，
 * 支持分页查询、详情查询、按股票代码查询、新增/批量新增等操作。
 *
 * @author baiyunpeng04
 * @since 2025/07/02
 */
@RestController
@RequestMapping("/api/financial-reports")
public class FinancialReportController {

    private final FinancialReportDomainService financialReportDomainService;

    public FinancialReportController(FinancialReportDomainService financialReportDomainService) {
        this.financialReportDomainService = financialReportDomainService;
    }

    /**
     * 分页查询财务报表。
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
    @GetMapping
    public ResponseDTO<PageResult<FinancialReportDTO>> query(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer fiscalYear,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        PageResult<FinancialReport> pageResult = financialReportDomainService.query(
                code, reportType, startDate, endDate, fiscalYear, page, size);

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
     * 根据 ID 获取财报详情。
     *
     * @param id 财报 ID
     * @return 财报详情
     */
    @GetMapping("/detail")
    public ResponseDTO<FinancialReportDTO> detail(@RequestParam Long id) {
        FinancialReport report = financialReportDomainService.getById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_PARAM.getCode(), "财报不存在: id=" + id);
        }
        return ResponseDTO.success(DtoConverter.toDto(report));
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
     * 根据股票代码和报表类型查询财报列表。
     *
     * <p>按报告日期降序排列。
     *
     * @param code       股票代码
     * @param reportType 报表类型（可选）
     * @return 财报列表
     */
    @GetMapping("/list-by-code")
    public ResponseDTO<List<FinancialReportDTO>> listByCode(
            @RequestParam String code,
            @RequestParam(required = false) String reportType) {
        List<FinancialReport> reports = financialReportDomainService.getByCodeAndType(code, reportType);
        List<FinancialReportDTO> dtos = reports.stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
        return ResponseDTO.success(dtos);
    }

    /**
     * 创建或更新财务报表。
     *
     * <p>使用 upsert 语义：存在相同唯一键的记录则更新，否则插入。
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

    /**
     * 批量创建财务报表。
     *
     * @param requests 财报保存请求列表
     * @return 成功保存的数量
     */
    @PostMapping("/batch")
    public ResponseDTO<Integer> batchSave(@RequestBody @Valid List<@Valid FinancialReportSaveRequest> requests) {
        List<FinancialReport> reports = requests.stream()
                .map(DtoConverter::toDomain)
                .collect(Collectors.toList());
        List<FinancialReport> saved = financialReportDomainService.batchSave(reports);
        return ResponseDTO.success(saved.size());
    }
}
