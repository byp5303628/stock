package com.ethanpark.stock.web;

import com.ethanpark.stock.biz.task.TaskConsumer;
import com.ethanpark.stock.biz.task.handler.HistoryStrategyTaskHandler;
import com.ethanpark.stock.common.dal.mappers.TaskMapper;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.StockBasicDomainService;
import com.ethanpark.stock.core.service.TaskDomainService;
import com.ethanpark.stock.remote.HistoryStockClient;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/8
 */
@SpringBootTest(classes = WebStarter.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IntegrationTest {

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskConsumer taskConsumer;

    @Resource
    private HistoryStockClient historyStockClient;

    @Resource
    private StockBasicDomainService stockBasicDomainService;

    @Resource
    private HistoryStrategyTaskHandler historyStrategyTaskHandler;
    @Autowired
    private TaskDomainService taskDomainService;

    @Test
    public void test3() throws Exception {
        List<String> list = FileUtils.readLines(new File("/Users/baiyunpeng04/workspace/stock/code.csv"), "utf-8");

        for (String line : list) {
            Task nextTask = new Task();
            nextTask.getContext().put("code", line);

            nextTask.setTaskType("HistoryStrategyTaskHandler");

            taskDomainService.save(nextTask);
        }
    }
}
