package com.ethanpark.stock.web;

import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.TaskDomainService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/8
 */
@SpringBootTest(classes = WebStarter.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IntegrationTest {

    @Resource
    private TaskDomainService taskDomainService;

    @Test
    public void test1() throws Exception {
        Task nextTask = new Task();
        nextTask.setTaskType("HistoryRegressionTaskHandler");
        nextTask.getContext().put("code", "600159");
        nextTask.getContext().put("startDate", "2020-01-01");
        nextTask.getContext().put("endDate", "2020-12-31");

        boolean save = taskDomainService.save(nextTask);
        Assert.assertTrue(save);
    }

}
