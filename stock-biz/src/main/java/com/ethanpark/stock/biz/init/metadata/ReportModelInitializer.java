package com.ethanpark.stock.biz.init.metadata;

import com.ethanpark.stock.biz.init.FieldDef;
import com.ethanpark.stock.biz.init.MetadataInitializer;

import java.util.List;

/**
 * 财务报告路由模型初始化。
 *
 * <p>该模型没有业务字段，仅用于 {@code RouteDispatcher} 路由查找：
 * dataType = "report" 时通过 metadata_model.code = "report" 确认合法性，
 * 然后路由到 {@code fin_report} 物理表。具体财报字段在各财务报表模型中定义。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
public class ReportModelInitializer implements MetadataInitializer {

    @Override
    public String getCode() {
        return "report";
    }

    @Override
    public String getName() {
        return "财务报表";
    }

    @Override
    public String getDescription() {
        return "财务报告聚合类型，用于路由查找，字段在各报表模型中定义";
    }

    @Override
    public List<FieldDef> getFields() {
        return List.of();
    }
}
