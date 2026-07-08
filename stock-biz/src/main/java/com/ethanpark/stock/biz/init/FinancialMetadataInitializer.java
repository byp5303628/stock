package com.ethanpark.stock.biz.init;

import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.MetadataDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * 财务报表元数据初始化器。
 *
 * <p>在应用启动时自动创建三大财务报表的元数据模型及字段定义。
 * 幂等设计：通过 code 检查模型是否存在，已存在则跳过，可重复执行。
 *
 * @author baiyunpeng04
 */
@Component
public class FinancialMetadataInitializer {

    private static final Logger log = LoggerFactory.getLogger(FinancialMetadataInitializer.class);

    private static final String MODEL_TYPE = "financial_statement";

    private final MetadataDomainService metadataDomainService;

    public FinancialMetadataInitializer(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }

    @PostConstruct
    public void init() {
        initCashFlowStatement();
        initBalanceSheet();
        initIncomeStatement();
        log.info("财务报表元数据初始化完成");
    }

    // ===== 现金流量表 =====

    private void initCashFlowStatement() {
        String code = "cash_flow_statement";
        if (metadataDomainService.getModelByCode(code) != null) {
            log.info("模型已存在，跳过: {}", code);
            return;
        }

        MetadataModel model = createModel(code, "现金流量表",
                "企业现金及现金等价物的流入、流出及净额变动情况");

        saveField(model.getId(), "net_cash_flow_operating", "经营活动现金流量净额", 1);
        saveField(model.getId(), "net_cash_flow_investing", "投资活动现金流量净额", 2);
        saveField(model.getId(), "net_cash_flow_financing", "筹资活动现金流量净额", 3);
        saveField(model.getId(), "cash_sales_received", "销售商品、提供劳务收到的现金", 4);
        saveField(model.getId(), "cash_paid_for_goods", "购买商品、接受劳务支付的现金", 5);
        saveField(model.getId(), "cash_paid_for_employees", "支付给职工以及为职工支付的现金", 6);
        saveField(model.getId(), "cash_paid_for_taxes", "支付的各项税费", 7);
        saveField(model.getId(), "net_increase_in_cash", "现金及现金等价物净增加额", 8);
        saveField(model.getId(), "cash_equivalent_beginning", "期初现金及现金等价物余额", 9);
        saveField(model.getId(), "cash_equivalent_ending", "期末现金及现金等价物余额", 10);

        log.info("现金流量表元数据初始化完成，模型ID: {}", model.getId());
    }

    // ===== 资产负债表 =====

    private void initBalanceSheet() {
        String code = "balance_sheet";
        if (metadataDomainService.getModelByCode(code) != null) {
            log.info("模型已存在，跳过: {}", code);
            return;
        }

        MetadataModel model = createModel(code, "资产负债表",
                "企业在某一特定日期的资产、负债和所有者权益状况");

        saveField(model.getId(), "total_assets", "资产总计", 1);
        saveField(model.getId(), "current_assets", "流动资产合计", 2);
        saveField(model.getId(), "cash_and_equivalents", "货币资金", 3);
        saveField(model.getId(), "accounts_receivable", "应收账款", 4);
        saveField(model.getId(), "inventory", "存货", 5);
        saveField(model.getId(), "non_current_assets", "非流动资产合计", 6);
        saveField(model.getId(), "fixed_assets", "固定资产", 7);
        saveField(model.getId(), "intangible_assets", "无形资产", 8);
        saveField(model.getId(), "total_liabilities", "负债合计", 9);
        saveField(model.getId(), "current_liabilities", "流动负债合计", 10);
        saveField(model.getId(), "non_current_liabilities", "非流动负债合计", 11);
        saveField(model.getId(), "accounts_payable", "应付账款", 12);
        saveField(model.getId(), "total_equity", "所有者权益合计", 13);
        saveField(model.getId(), "share_capital", "股本", 14);
        saveField(model.getId(), "retained_earnings", "未分配利润", 15);

        log.info("资产负债表元数据初始化完成，模型ID: {}", model.getId());
    }

    // ===== 利润表 =====

    private void initIncomeStatement() {
        String code = "income_statement";
        if (metadataDomainService.getModelByCode(code) != null) {
            log.info("模型已存在，跳过: {}", code);
            return;
        }

        MetadataModel model = createModel(code, "利润表",
                "企业在一定会计期间的经营成果");

        saveField(model.getId(), "total_revenue", "营业总收入", 1);
        saveField(model.getId(), "operating_revenue", "营业收入", 2);
        saveField(model.getId(), "total_cogs", "营业总成本", 3);
        saveField(model.getId(), "operating_cost", "营业成本", 4);
        saveField(model.getId(), "selling_expense", "销售费用", 5);
        saveField(model.getId(), "admin_expense", "管理费用", 6);
        saveField(model.getId(), "r_d_expense", "研发费用", 7);
        saveField(model.getId(), "financial_expense", "财务费用", 8);
        saveField(model.getId(), "operating_profit", "营业利润", 9);
        saveField(model.getId(), "total_profit", "利润总额", 10);
        saveField(model.getId(), "income_tax", "所得税费用", 11);
        saveField(model.getId(), "net_profit", "净利润", 12);
        saveField(model.getId(), "net_profit_parent", "归属于母公司所有者的净利润", 13);
        saveField(model.getId(), "basic_eps", "基本每股收益", 14);
        saveField(model.getId(), "diluted_eps", "稀释每股收益", 15);

        log.info("利润表元数据初始化完成，模型ID: {}", model.getId());
    }

    // ===== 私有辅助方法 =====

    /**
     * 创建元数据模型。
     *
     * @param code        模型编码
     * @param name        模型名称
     * @param description 模型描述
     * @return 已保存的模型对象（含 ID）
     */
    private MetadataModel createModel(String code, String name, String description) {
        MetadataModel model = new MetadataModel();
        model.setCode(code);
        model.setName(name);
        model.setModelType(MODEL_TYPE);
        model.setDescription(description);
        model.setExtInfo(Map.of("dataType", "report"));

        var result = metadataDomainService.saveModel(model);
        if (!result.isSuccess()) {
            log.error("创建元数据模型失败: code={}, msg={}", code, result.getMsg());
            throw new IllegalStateException("创建元数据模型失败: " + code + ", " + result.getMsg());
        }
        return result.getData();
    }

    /**
     * 创建并保存元数据字段。
     *
     * @param modelId        所属模型 ID
     * @param fieldName       字段编码名
     * @param businessMeaning 业务含义（中文）
     * @param sortOrder       排序序号
     */
    private void saveField(Long modelId, String fieldName, String businessMeaning, int sortOrder) {
        MetadataField field = new MetadataField();
        field.setModelId(modelId);
        field.setFieldName(fieldName);
        field.setFieldType("DECIMAL");
        field.setBusinessMeaning(businessMeaning);
        field.setSortOrder(sortOrder);

        var result = metadataDomainService.saveField(field);
        if (!result.isSuccess()) {
            log.error("创建元数据字段失败: modelId={}, fieldName={}, msg={}",
                    modelId, fieldName, result.getMsg());
            throw new IllegalStateException("创建元数据字段失败: " + fieldName + ", " + result.getMsg());
        }
    }
}
