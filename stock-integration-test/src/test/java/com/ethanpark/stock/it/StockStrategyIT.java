package com.ethanpark.stock.it;

import com.ethanpark.stock.biz.controller.StockStrategyController;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.dto.StockRegressionDetailDTO;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.TaskDomainService;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;

/**
 * 股票策略集成测试。
 *
 * <p>启动完整 Spring Boot 上下文，验证策略查询和任务创建流程。
 *
 * @author baiyunpeng04
 * @since 2024/11/8
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
public class StockStrategyIT {

    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private List<TradePolicy> tradePolicies;

    @Autowired
    private StockStrategyController stockStrategyController;

    @Test
    public void testWeb() {
        ResponseDTO<StockRegressionDetailDTO> macdDailyV1TradePolicy = stockStrategyController.getStockAndPolicyDetail("MacdDailyV1TradePolicy", "000973");

        Assertions.assertNotNull(macdDailyV1TradePolicy);
    }

    @Test
    public void test3() throws Exception {
        List<String> list = FileUtils.readLines(new File("/Users/baiyunpeng04/workspace/stock" +
                "/code.csv"), "utf-8");

        for (String line : list) {
            Task nextTask = new Task();
            nextTask.getContext().put("code", line);

            nextTask.setTaskType("HistoryStrategyTaskHandler");

            taskDomainService.save(nextTask);
        }
    }

    @Test
    public void test1() throws Exception {
        String code = "600085";

        ResponseDTO<StockRegressionDetailDTO> stockAndPolicyDetail = stockStrategyController.getStockAndPolicyDetail("MacdDailyV1TradePolicy", code);

        Assertions.assertNotNull(stockAndPolicyDetail);
    }
}
