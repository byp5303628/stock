package com.ethanpark.stock.biz.cal;

import com.ethanpark.stock.biz.cal.impl.MacdStatStatisticsStrategy;
import com.ethanpark.stock.biz.cal.impl.MonthStatStatisticsStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
@Service
public class StatisticsStrategyFactory {
    private List<StatisticsStrategy> strategies = new ArrayList<>();

    @PostConstruct
    public void init() {
        strategies.add(new MonthStatStatisticsStrategy());
        strategies.add(new MacdStatStatisticsStrategy());
    }


    public List<StatisticsStrategy> getStrategies() {
        return strategies;
    }
}
