package com.ethanpark.stock.biz.init.metadata;

import com.ethanpark.stock.biz.init.FieldDef;
import com.ethanpark.stock.biz.init.MetadataInitializer;

import java.util.List;

/**
 * 现金流量表元数据初始化。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
public class CashFlowStatementInitializer implements MetadataInitializer {

    @Override
    public String getCode() {
        return "cash_flow_statement";
    }

    @Override
    public String getName() {
        return "现金流量表";
    }

    @Override
    public String getDescription() {
        return "企业现金流状况";
    }

    @Override
    public List<FieldDef> getFields() {
        return List.of(
                FieldDef.of("net_cash_flow_operating", "经营活动现金流量净额", 1),
                FieldDef.of("net_cash_flow_investing", "投资活动现金流量净额", 2),
                FieldDef.of("net_cash_flow_financing", "筹资活动现金流量净额", 3),
                FieldDef.of("cash_sales_received", "销售商品、提供劳务收到的现金", 4),
                FieldDef.of("cash_paid_for_goods", "购买商品、接受劳务支付的现金", 5),
                FieldDef.of("cash_paid_for_employees", "支付给职工以及为职工支付的现金", 6),
                FieldDef.of("cash_paid_for_taxes", "支付的各项税费", 7),
                FieldDef.of("net_increase_in_cash", "现金及现金等价物净增加额", 8),
                FieldDef.of("cash_equivalent_beginning", "期初现金及现金等价物余额", 9),
                FieldDef.of("cash_equivalent_ending", "期末现金及现金等价物余额", 10)
        );
    }
}
