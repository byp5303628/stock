package com.ethanpark.stock.biz.init;

import com.ethanpark.stock.biz.init.metadata.BalanceSheetInitializer;
import com.ethanpark.stock.biz.init.metadata.CashFlowStatementInitializer;
import com.ethanpark.stock.biz.init.metadata.IncomeStatementInitializer;
import com.ethanpark.stock.biz.init.metadata.ReportModelInitializer;
import lombok.Getter;

/**
 * 初始化器定义枚举。
 *
 * <p>轻量注册表，枚举项只描述"有什么初始化任务"和"由哪个类实现"。
 * 执行逻辑由 {@link InitializerRunner} 统一调度。
 *
 * <p>使用方式：
 * <ul>
 *   <li>新增元数据模型 → 实现 {@link MetadataInitializer} 接口，在枚举中加一项</li>
 *   <li>新增爬虫任务 → 在枚举中加 {@link Category#CRAWLER} 项，handler 自行管理</li>
 * </ul>
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
@Getter
public enum InitializerDefinition {

    // ===== 元数据模型初始化 =====

    /** 财务报告聚合类型，用于 RouteDispatcher 路由查找 */
    REPORT(Category.METADATA, ReportModelInitializer.class),
    CASH_FLOW_STATEMENT(Category.METADATA, CashFlowStatementInitializer.class),
    BALANCE_SHEET(Category.METADATA, BalanceSheetInitializer.class),
    INCOME_STATEMENT(Category.METADATA, IncomeStatementInitializer.class),

    // ===== 数据爬取任务（扩展预留） =====
    // FINANCIAL_REPORT_CRAWLER(Category.CRAWLER, MyCrawlerHandler.class),
    ;

    private final Category category;
    private final Class<?> handlerClass;

    InitializerDefinition(Category category, Class<?> handlerClass) {
        this.category = category;
        this.handlerClass = handlerClass;
    }

    /**
     * 初始化类别。
     */
    public enum Category {
        METADATA,
        CRAWLER,
    }
}
