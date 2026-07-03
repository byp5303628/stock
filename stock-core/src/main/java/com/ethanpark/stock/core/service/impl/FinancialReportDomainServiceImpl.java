package com.ethanpark.stock.core.service.impl;

import com.ethanpark.stock.common.dal.mappers.FinancialReportMapper;
import com.ethanpark.stock.common.dal.mappers.entity.FinancialReportDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.FinancialReport;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.FinancialReportDomainService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 财报领域服务实现。
 *
 * @author baiyunpeng04
 * @since 2025/07/02
 */
@Service
public class FinancialReportDomainServiceImpl implements FinancialReportDomainService {

    private static final Set<String> VALID_REPORT_TYPES = Set.of("CASH_FLOW", "BALANCE", "INCOME");

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final FinancialReportMapper financialReportMapper;

    public FinancialReportDomainServiceImpl(FinancialReportMapper financialReportMapper) {
        this.financialReportMapper = financialReportMapper;
    }

    @Override
    public FinancialReport save(FinancialReport report) {
        validate(report);

        FinancialReportDO dbEntity = DbConverter.toDbEntity(report);
        financialReportMapper.upsert(dbEntity);

        // 对于新增记录，upsert 已通过 useGeneratedKeys 回写 id
        // 对于已有记录，保留原始 id
        Long id = dbEntity.getId() != null ? dbEntity.getId() : report.getId();
        if (id == null || id == 0) {
            throw new IllegalStateException("保存财报后无法获取 ID: code=" + report.getCode()
                    + ", reportType=" + report.getReportType());
        }

        return DomainConverter.toDomain(financialReportMapper.selectById(id));
    }

    @Override
    public List<FinancialReport> batchSave(List<FinancialReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }

        List<FinancialReport> saved = new ArrayList<>(reports.size());
        for (FinancialReport report : reports) {
            saved.add(save(report));
        }
        return saved;
    }

    @Override
    public PageResult<FinancialReport> query(String code, String reportType,
                                             String startDate, String endDate,
                                             Integer fiscalYear, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
        }

        int offset = (page - 1) * size;
        long total = financialReportMapper.count(code, reportType, startDate, endDate, fiscalYear);

        List<FinancialReportDO> dos;
        if (total > 0) {
            dos = financialReportMapper.selectPage(code, reportType, startDate, endDate,
                    fiscalYear, offset, size);
        } else {
            dos = Collections.emptyList();
        }

        List<FinancialReport> items = dos.stream()
                .map(DomainConverter::toDomain)
                .collect(Collectors.toList());

        PageResult<FinancialReport> result = new PageResult<>();
        result.setItems(items);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setPages((int) Math.ceil((double) total / size));

        return result;
    }

    @Override
    public FinancialReport getById(Long id) {
        if (id == null) {
            return null;
        }
        FinancialReportDO dbEntity = financialReportMapper.selectById(id);
        return DomainConverter.toDomain(dbEntity);
    }

    @Override
    public List<FinancialReport> getLatestByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<FinancialReportDO> dos = financialReportMapper.selectLatestByCode(code);
        return dos.stream()
                .map(DomainConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialReport> getByCodeAndType(String code, String reportType) {
        if (code == null || code.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<FinancialReportDO> dos = financialReportMapper.selectByCodeAndType(code, reportType);
        return dos.stream()
                .map(DomainConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        financialReportMapper.deleteById(id);
    }

    /**
     * 校验财报必填字段。
     *
     * @param report 财报领域对象
     * @throws IllegalArgumentException 校验不通过时抛出
     */
    private void validate(FinancialReport report) {
        if (report == null) {
            throw new IllegalArgumentException("财报对象不能为空");
        }
        if (report.getCode() == null || report.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("股票代码不能为空");
        }
        if (report.getReportType() == null || !VALID_REPORT_TYPES.contains(report.getReportType())) {
            throw new IllegalArgumentException("报表类型不合法: " + report.getReportType()
                    + "，合法值为 " + VALID_REPORT_TYPES);
        }
    }
}
