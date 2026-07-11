package com.ethanpark.stock.biz.init.metadata;

import com.ethanpark.stock.biz.init.MetadataInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MetadataInitializer 实现类的字段定义正确性验证。
 */
class MetadataInitializerTest {

    @Test
    @DisplayName("现金流量表 — code/name/description 正确")
    void cashFlowStatement_metadata() {
        CashFlowStatementInitializer init = new CashFlowStatementInitializer();

        assertThat(init.getCode()).isEqualTo("cash_flow_statement");
        assertThat(init.getName()).isEqualTo("现金流量表");
        assertThat(init.getDescription()).isEqualTo("企业现金流状况");
    }

    @Test
    @DisplayName("现金流量表 — 10 个字段，每个字段有正确的名称和含义")
    void cashFlowStatement_fields() {
        CashFlowStatementInitializer init = new CashFlowStatementInitializer();

        assertThat(init.getFields()).hasSize(10);
        assertThat(init.getFields())
                .extracting("fieldName")
                .contains("net_cash_flow_operating", "net_cash_flow_investing",
                        "cash_equivalent_beginning", "cash_equivalent_ending");
    }

    @Test
    @DisplayName("资产负债表 — code/name/description 正确")
    void balanceSheet_metadata() {
        BalanceSheetInitializer init = new BalanceSheetInitializer();

        assertThat(init.getCode()).isEqualTo("balance_sheet");
        assertThat(init.getName()).isEqualTo("资产负债表");
        assertThat(init.getDescription()).isEqualTo("企业资产负债状况");
    }

    @Test
    @DisplayName("资产负债表 — 15 个字段，sortOrder 从 1 递增")
    void balanceSheet_fields() {
        BalanceSheetInitializer init = new BalanceSheetInitializer();

        assertThat(init.getFields()).hasSize(15);
        assertThat(init.getFields())
                .extracting("sortOrder")
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    }

    @Test
    @DisplayName("利润表 — code/name/description 正确")
    void incomeStatement_metadata() {
        IncomeStatementInitializer init = new IncomeStatementInitializer();

        assertThat(init.getCode()).isEqualTo("income_statement");
        assertThat(init.getName()).isEqualTo("利润表");
        assertThat(init.getDescription()).isEqualTo("企业经营成果");
    }

    @Test
    @DisplayName("利润表 — 15 个字段，包含经营利润/净利润/每股收益")
    void incomeStatement_fields() {
        IncomeStatementInitializer init = new IncomeStatementInitializer();

        assertThat(init.getFields()).hasSize(15);
        assertThat(init.getFields())
                .extracting("fieldName")
                .contains("operating_profit", "net_profit", "basic_eps", "diluted_eps");
    }

    @Test
    @DisplayName("report 路由模型 — code/description 正确，无字段")
    void reportModel_metadata() {
        ReportModelInitializer init = new ReportModelInitializer();

        assertThat(init.getCode()).isEqualTo("report");
        assertThat(init.getName()).isEqualTo("财务报表");
        assertThat(init.getFields()).isEmpty();
    }

    @Test
    @DisplayName("所有 MetadataInitializer 实现的 code 不重复")
    void allCodesAreUnique() {
        List<MetadataInitializer> initializers = List.of(
                new ReportModelInitializer(),
                new CashFlowStatementInitializer(),
                new BalanceSheetInitializer(),
                new IncomeStatementInitializer()
        );

        List<String> codes = initializers.stream()
                .map(MetadataInitializer::getCode)
                .toList();

        assertThat(codes).doesNotHaveDuplicates();
    }
}
