package com.ethanpark.stock.biz.cal;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
@Service
public class StatisticsStrategyFactory {
    @Resource
    private List<StatisticsStrategy> strategies;


    public List<StatisticsStrategy> getStrategies() {
        return strategies;
    }
}
