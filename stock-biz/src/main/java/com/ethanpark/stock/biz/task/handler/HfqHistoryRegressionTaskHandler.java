package com.ethanpark.stock.biz.task.handler;

import com.ethanpark.stock.common.util.DateUtils;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.StockBasicDomainService;
import com.ethanpark.stock.remote.HistoryStockClient;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Service
public class HfqHistoryRegressionTaskHandler extends BaseTaskHandler {
    @Resource
    private HistoryStockClient historyStockClient;

    @Resource
    private StockBasicDomainService stockBasicDomainService;

    @Override
    protected Result<Void> handle0(Task task) {
        Map<String, String> context = task.getContext();

        String code = context.get("code");
        String startDate = context.get("startDate");
        String endDate = context.get("endDate");

        List<StockBasic> stockBasics = historyStockClient.queryStockHistory(code, startDate,
                endDate, false);

        for (StockBasic stockBasic : stockBasics) {
            boolean b = stockBasicDomainService.saveHfqStockBasic(stockBasic);

            if (!b) {
                return Result.fail("数据存储存在问题, 需要进行重试!");
            }
        }

        // 创建下一个任务
        String nextStartDate = DateUtils.plusYear(startDate);

        if (!CollectionUtils.isEmpty(stockBasics)) {
            String lastPartitionDate = stockBasics.get(stockBasics.size() - 1).getPartitionDate();

            if (nextStartDate.compareTo(lastPartitionDate) > 0) {
                nextStartDate = DateUtils.plusDay(lastPartitionDate, 1);;
            }
        }


        String nextEndDate = DateUtils.plusYear(endDate);

        Task nextTask = new Task();
        nextTask.setTaskType(getTaskType());
        nextTask.setExternalSerialNo(task.getId());
        nextTask.getContext().put("code", code);
        nextTask.getContext().put("startDate", nextStartDate);
        nextTask.getContext().put("endDate", nextEndDate);

        nextTask.setFireTime(DateUtils.parseStringToDate(nextStartDate, "yyyy-MM-dd"));

        taskDomainService.save(nextTask);

        return Result.ok();
    }

    public static void main(String[] args) {
        String a = "2024-05-01";
        String b = "2024-04-01";


        LocalDateTime localDateTime = DateUtils.parseStringToDate(a, "yyyy-MM-dd");

        System.out.println(localDateTime);
    }
}
