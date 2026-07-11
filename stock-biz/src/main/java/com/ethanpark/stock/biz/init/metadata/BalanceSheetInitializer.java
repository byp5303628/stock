package com.ethanpark.stock.biz.init.metadata;

import com.ethanpark.stock.biz.init.FieldDef;
import com.ethanpark.stock.biz.init.MetadataInitializer;

import java.util.List;

/**
 * 资产负债表元数据初始化。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
public class BalanceSheetInitializer implements MetadataInitializer {

    @Override
    public String getCode() {
        return "balance_sheet";
    }

    @Override
    public String getName() {
        return "资产负债表";
    }

    @Override
    public String getDescription() {
        return "企业资产负债状况";
    }

    @Override
    public List<FieldDef> getFields() {
        return List.of(
                FieldDef.of("total_assets", "资产总计", 1),
                FieldDef.of("current_assets", "流动资产合计", 2),
                FieldDef.of("cash_and_equivalents", "货币资金", 3),
                FieldDef.of("accounts_receivable", "应收账款", 4),
                FieldDef.of("inventory", "存货", 5),
                FieldDef.of("non_current_assets", "非流动资产合计", 6),
                FieldDef.of("fixed_assets", "固定资产", 7),
                FieldDef.of("intangible_assets", "无形资产", 8),
                FieldDef.of("total_liabilities", "负债合计", 9),
                FieldDef.of("current_liabilities", "流动负债合计", 10),
                FieldDef.of("non_current_liabilities", "非流动负债合计", 11),
                FieldDef.of("accounts_payable", "应付账款", 12),
                FieldDef.of("total_equity", "所有者权益合计", 13),
                FieldDef.of("share_capital", "股本", 14),
                FieldDef.of("retained_earnings", "未分配利润", 15)
        );
    }
}
