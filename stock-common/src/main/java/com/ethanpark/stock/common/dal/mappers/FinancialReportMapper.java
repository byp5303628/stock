package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.FinancialReportDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for financial_report table.
 *
 * @author baiyunpeng04
 * @since 2024/11/7
 */
public interface FinancialReportMapper {

    int insert(FinancialReportDO entity);

    int upsert(FinancialReportDO entity);

    int updateById(FinancialReportDO entity);

    FinancialReportDO selectById(@Param("id") Long id);

    List<FinancialReportDO> selectPage(
            @Param("code") String code,
            @Param("reportType") String reportType,
            @Param("reportPeriod") String reportPeriod,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("fiscalYear") Integer fiscalYear,
            @Param("offset") int offset,
            @Param("limit") int limit);

    Long count(
            @Param("code") String code,
            @Param("reportType") String reportType,
            @Param("reportPeriod") String reportPeriod,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("fiscalYear") Integer fiscalYear);

    FinancialReportDO selectByUniqueKey(
            @Param("code") String code,
            @Param("reportType") String reportType,
            @Param("reportDate") String reportDate);

    List<FinancialReportDO> selectLatestByCode(@Param("code") String code);

    int deleteById(@Param("id") Long id);
}
