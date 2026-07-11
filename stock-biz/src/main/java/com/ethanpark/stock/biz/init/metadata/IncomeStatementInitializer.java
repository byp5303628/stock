package com.ethanpark.stock.biz.init.metadata;

import com.ethanpark.stock.biz.init.FieldDef;
import com.ethanpark.stock.biz.init.MetadataInitializer;

import java.util.List;

/**
 * 利润表元数据初始化。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
public class IncomeStatementInitializer implements MetadataInitializer {

    @Override
    public String getCode() {
        return "income_statement";
    }

    @Override
    public String getName() {
        return "利润表";
    }

    @Override
    public String getDescription() {
        return "企业经营成果";
    }

    @Override
    public List<FieldDef> getFields() {
        return List.of(
                FieldDef.of("total_revenue", "营业总收入", 1),
                FieldDef.of("operating_revenue", "营业收入", 2),
                FieldDef.of("total_cogs", "营业总成本", 3),
                FieldDef.of("operating_cost", "营业成本", 4),
                FieldDef.of("selling_expense", "销售费用", 5),
                FieldDef.of("admin_expense", "管理费用", 6),
                FieldDef.of("r_d_expense", "研发费用", 7),
                FieldDef.of("financial_expense", "财务费用", 8),
                FieldDef.of("operating_profit", "营业利润", 9),
                FieldDef.of("total_profit", "利润总额", 10),
                FieldDef.of("income_tax", "所得税费用", 11),
                FieldDef.of("net_profit", "净利润", 12),
                FieldDef.of("net_profit_parent", "归属于母公司所有者的净利润", 13),
                FieldDef.of("basic_eps", "基本每股收益", 14),
                FieldDef.of("diluted_eps", "稀释每股收益", 15)
        );
    }
}
