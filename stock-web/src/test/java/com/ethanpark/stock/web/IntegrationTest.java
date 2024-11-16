package com.ethanpark.stock.web;

import com.ethanpark.stock.biz.cal.impl.MonthStatStatisticsStrategy;
import com.ethanpark.stock.biz.task.TaskConsumer;
import com.ethanpark.stock.common.dal.mappers.TaskMapper;
import com.ethanpark.stock.common.dal.mappers.entity.TaskDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.remote.HistoryStockClient;
import com.ethanpark.stock.remote.model.StockBasic;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    @Test
    public void test1() throws Exception {
        Task nextTask = new Task();
        nextTask.setTaskType("HfqHistoryRegressionTaskHandler");
        nextTask.getContext().put("code", "600159");
        nextTask.getContext().put("startDate", "2020-01-01");
        nextTask.getContext().put("endDate", "2020-12-31");

        TaskDO taskDO = DbConverter.toDbEntity(nextTask);

        int insert = taskMapper.insert(taskDO);

        Assert.assertTrue(insert > 0);
    }

    @Test
    public void test() throws Exception {
        List<String> lines = FileUtils.readLines(new File("/Users/baiyunpeng04/workspace/stock/code.csv"), "utf-8");

        for (String code : lines) {
            Task nextTask = new Task();
            nextTask.setTaskType("HfqHistoryRegressionTaskHandler");
            nextTask.getContext().put("code", code);
            nextTask.getContext().put("startDate", "2005-01-01");
            nextTask.getContext().put("endDate", "2005-12-31");

            TaskDO taskDO = DbConverter.toDbEntity(nextTask);

            int insert = taskMapper.insert(taskDO);

            Assert.assertTrue(insert > 0);
        }

        Assert.assertNotNull(lines);
    }

    @Test
    public void test3() throws Exception {
        taskConsumer.consume(1L);
    }

    @Test
    public void test2() throws Exception {
        List<StockBasic> stockBasics = historyStockClient.queryStockHistory("300071", "2024-01-01", "2024-12-31", true);

        MonthStatStatisticsStrategy
                strategy = new MonthStatStatisticsStrategy();

        List<StockStatistics> calculate = strategy.calculate(stockBasics);

        Assert.assertNotNull(calculate);
    }

}
