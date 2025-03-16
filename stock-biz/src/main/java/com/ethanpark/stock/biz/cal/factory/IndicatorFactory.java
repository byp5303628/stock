package com.ethanpark.stock.biz.cal.factory;

import com.ethanpark.stock.biz.cal.factory.strategyindicators.StrategyIndicatorCalculator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Service
public class IndicatorFactory {

    @Resource
    private List<StrategyIndicatorCalculator> strategyCalculatorList;

    public List<StrategyIndicatorCalculator> selectStrategyCalculators() {
        return strategyCalculatorList;
    }
}
