package com.ethanpark.stock.biz.cal;

import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.remote.model.StockBasic;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/13
 */
public interface StatisticsStrategy {

    /**
     * 根据一系列基础指标计算的统计指标
     *
     * @param stockBasicList
     * @return
     */
    List<StockStatistics> calculate(List<StockBasic> stockBasicList);

    StatisticsType getStatisticsType();

    default String getName() {
        return getStatisticsType().name();
    }
}
