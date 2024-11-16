package com.ethanpark.stock.biz.task.handler;

import com.ethanpark.stock.biz.cal.StatisticsStrategy;
import com.ethanpark.stock.biz.cal.StatisticsStrategyFactory;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.core.model.Task;
import com.ethanpark.stock.core.service.StockBasicDomainService;
import com.ethanpark.stock.core.service.StockStatisticsDomainService;
import com.ethanpark.stock.remote.model.StockBasic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/16
 */
@Slf4j
@Service
public class HistoryStrategyTaskHandler extends BaseTaskHandler {

    @Resource
    private StockBasicDomainService stockBasicDomainService;

    @Resource
    private StockStatisticsDomainService stockStatisticsDomainService;

    @Resource
    private StatisticsStrategyFactory statisticsStrategyFactory;

    @Override
    protected Result<Void> handle0(Task task) {

        Map<String, String> context = task.getContext();
        String code = context.get("code");

        List<StockBasic> stockBasics = stockBasicDomainService.queryAllHfqStockBasics(code);

        List<StatisticsStrategy> strategies = statisticsStrategyFactory.getStrategies();

        for (StatisticsStrategy strategy : strategies) {
            List<StockStatistics> statList = strategy.calculate(stockBasics);

            Result<Void> result = stockStatisticsDomainService.batchSave(statList);

            if (!result.isSuccess()) {
                log.error("保存数据失败! statCode={}, name={}", code, strategy.getName());
                return result;
            }
        }

        return Result.ok();
    }
}
