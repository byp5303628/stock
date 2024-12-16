package com.ethanpark.stock.web;

import com.ethanpark.stock.biz.controller.StockStrategyController;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.dto.StockRegressionDetailDTO;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.model.TradeContext;
import com.ethanpark.stock.core.service.TaskDomainService;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/8
 */
@SpringBootTest(classes = WebStarter.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IntegrationTest {


    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private List<TradePolicy> tradePolicies;

    @Autowired
    private StockStrategyController stockStrategyController;

    @Test
    public void testWeb() {
        ResponseDTO<StockRegressionDetailDTO> macdDailyV1TradePolicy = stockStrategyController.getStockAndPolicyDetail("MacdDailyV1TradePolicy", "000973");

        Assert.assertNotNull(macdDailyV1TradePolicy);
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

        for (TradePolicy tradePolicy : tradePolicies) {
            TradeContext tradeContext = tradePolicy.trade(code);

            FileUtils.writeStringToFile(new File("/Users/baiyunpeng04/workspace/stock/reports/" + code + "." + tradePolicy.getClass().getSimpleName() + ".md"), tradeContext.genReport(), "utf-8");
        }
    }
}
