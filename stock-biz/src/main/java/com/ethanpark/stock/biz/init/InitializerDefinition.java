package com.ethanpark.stock.biz.init;

import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.MetadataDomainService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 初始化器定义枚举。
 *
 * <p>统一管理所有启动时执行的初始化任务，包括元数据模型创建、数据爬虫注册等。
 * 新增初始化任务只需在此枚举中添加一项，由 {@link InitializerRunner} 统一执行。
 *
 * <p>当前支持的初始化类型：
 * <ul>
 *   <li>{@link Category#METADATA} — 创建 metadata_model 及对应的 field 定义</li>
 *   <li>{@link Category#CRAWLER} — 注册/触发数据爬取任务（扩展预留）</li>
 * </ul>
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
@Slf4j
@Getter
public enum InitializerDefinition {

    // ===== 元数据模型初始化 =====

    CASH_FLOW_STATEMENT("cash_flow_statement", "现金流量表", "企业现金流状况", Category.METADATA,
            List.of(
                    field("net_cash_flow_operating", "经营活动现金流量净额", 1),
                    field("net_cash_flow_investing", "投资活动现金流量净额", 2),
                    field("net_cash_flow_financing", "筹资活动现金流量净额", 3),
                    field("cash_sales_received", "销售商品、提供劳务收到的现金", 4),
                    field("cash_paid_for_goods", "购买商品、接受劳务支付的现金", 5),
                    field("cash_paid_for_employees", "支付给职工以及为职工支付的现金", 6),
                    field("cash_paid_for_taxes", "支付的各项税费", 7),
                    field("net_increase_in_cash", "现金及现金等价物净增加额", 8),
                    field("cash_equivalent_beginning", "期初现金及现金等价物余额", 9),
                    field("cash_equivalent_ending", "期末现金及现金等价物余额", 10)
            )),

    BALANCE_SHEET("balance_sheet", "资产负债表", "企业资产负债状况", Category.METADATA,
            List.of(
                    field("total_assets", "资产总计", 1),
                    field("current_assets", "流动资产合计", 2),
                    field("cash_and_equivalents", "货币资金", 3),
                    field("accounts_receivable", "应收账款", 4),
                    field("inventory", "存货", 5),
                    field("non_current_assets", "非流动资产合计", 6),
                    field("fixed_assets", "固定资产", 7),
                    field("intangible_assets", "无形资产", 8),
                    field("total_liabilities", "负债合计", 9),
                    field("current_liabilities", "流动负债合计", 10),
                    field("non_current_liabilities", "非流动负债合计", 11),
                    field("accounts_payable", "应付账款", 12),
                    field("total_equity", "所有者权益合计", 13),
                    field("share_capital", "股本", 14),
                    field("retained_earnings", "未分配利润", 15)
            )),

    INCOME_STATEMENT("income_statement", "利润表", "企业经营成果", Category.METADATA,
            List.of(
                    field("total_revenue", "营业总收入", 1),
                    field("operating_revenue", "营业收入", 2),
                    field("total_cogs", "营业总成本", 3),
                    field("operating_cost", "营业成本", 4),
                    field("selling_expense", "销售费用", 5),
                    field("admin_expense", "管理费用", 6),
                    field("r_d_expense", "研发费用", 7),
                    field("financial_expense", "财务费用", 8),
                    field("operating_profit", "营业利润", 9),
                    field("total_profit", "利润总额", 10),
                    field("income_tax", "所得税费用", 11),
                    field("net_profit", "净利润", 12),
                    field("net_profit_parent", "归属于母公司所有者的净利润", 13),
                    field("basic_eps", "基本每股收益", 14),
                    field("diluted_eps", "稀释每股收益", 15)
            )),

    // ===== 数据爬取任务（扩展预留） =====
    // 示例：
    // FINANCIAL_REPORT_CRAWLER("financial_report_crawler", "财报爬虫",
    //         "从数据源获取最新财报", Category.CRAWLER, List.of()),
    ;

    /** 枚举项编码，对应 metadata_model.code 或爬虫任务标识 */
    private final String code;

    /** 名称 */
    private final String name;

    /** 描述 */
    private final String description;

    /** 初始化类别 */
    private final Category category;

    /** 字段定义列表（仅 METADATA 类型使用） */
    private final List<FieldDef> fields;

    InitializerDefinition(String code, String name, String description,
                          Category category, List<FieldDef> fields) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.fields = fields;
    }

    /**
     * 执行初始化。
     * <p>幂等设计：重复调用时跳过已存在的模型。
     *
     * @param metadataDomainService metadata 服务
     */
    public void initialize(MetadataDomainService metadataDomainService) {
        switch (category) {
            case METADATA:
                initMetadata(metadataDomainService);
                break;
            case CRAWLER:
                log.info("爬虫任务已注册: code={}, name={}", code, name);
                // 预留：后续在此处触发爬虫调度
                break;
        }
    }

    private static final String MODEL_TYPE = "financial_statement";

    private void initMetadata(MetadataDomainService metadataDomainService) {
        if (metadataDomainService.getModelByCode(code) != null) {
            log.info("模型已存在，跳过: {}", code);
            return;
        }

        // 创建模型
        MetadataModel model = new MetadataModel();
        model.setCode(code);
        model.setName(name);
        model.setModelType(MODEL_TYPE);
        model.setDescription(description);
        model.setExtInfo(Map.of("dataType", "report"));

        var modelResult = metadataDomainService.saveModel(model);
        if (!modelResult.isSuccess()) {
            log.error("创建元数据模型失败: code={}, msg={}", code, modelResult.getMsg());
            throw new IllegalStateException("创建元数据模型失败: " + code + ", " + modelResult.getMsg());
        }
        Long modelId = modelResult.getData().getId();

        // 创建字段
        for (FieldDef fieldDef : fields) {
            MetadataField field = new MetadataField();
            field.setModelId(modelId);
            field.setFieldName(fieldDef.getFieldName());
            field.setFieldType("DECIMAL");
            field.setBusinessMeaning(fieldDef.getBusinessMeaning());
            field.setSortOrder(fieldDef.getSortOrder());

            var fieldResult = metadataDomainService.saveField(field);
            if (!fieldResult.isSuccess()) {
                log.error("创建元数据字段失败: modelId={}, fieldName={}, msg={}",
                        modelId, fieldDef.getFieldName(), fieldResult.getMsg());
                throw new IllegalStateException(
                        "创建元数据字段失败: " + fieldDef.getFieldName() + ", " + fieldResult.getMsg());
            }
        }

        log.info("元数据模型初始化完成: code={}, name={}, 字段数={}", code, name, fields.size());
    }

    // ===== 内部类型 =====

    /**
     * 初始化类别。
     */
    public enum Category {
        /** 元数据模型创建 */
        METADATA,
        /** 数据爬取任务 */
        CRAWLER,
    }

    /**
     * 字段定义。
     */
    @Getter
    public static class FieldDef {
        private final String fieldName;
        private final String businessMeaning;
        private final int sortOrder;

        public FieldDef(String fieldName, String businessMeaning, int sortOrder) {
            this.fieldName = fieldName;
            this.businessMeaning = businessMeaning;
            this.sortOrder = sortOrder;
        }
    }

    // ===== 快捷工厂 =====

    private static FieldDef field(String fieldName, String businessMeaning, int sortOrder) {
        return new FieldDef(fieldName, businessMeaning, sortOrder);
    }
}
