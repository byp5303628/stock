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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
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
        nextTask.setTaskType("HistoryRegressionTaskHandler");
        nextTask.getContext().put("code", "600159");
        nextTask.getContext().put("startDate", "2020-01-01");
        nextTask.getContext().put("endDate", "2020-12-31");

        TaskDO taskDO = DbConverter.toDbEntity(nextTask);

        int insert = taskMapper.insert(taskDO);

        Assert.assertTrue(insert > 0);
    }

    @Test
    public void test2() throws Exception {
        List<StockBasic> stockBasics = historyStockClient.queryStockHistory("600159", "2020-01-01", "2020-12-31", true);

        MonthStatStatisticsStrategy
                strategy = new MonthStatStatisticsStrategy();

        List<StockStatistics> calculate = strategy.calculate(stockBasics);

        Assert.assertNotNull(calculate);
    }

}
