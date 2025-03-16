package com.ethanpark.stock.core.model;

import com.ethanpark.stock.remote.model.StockBasic;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Getter
@Setter
public class StockContext {
    private List<StockBasic> hfqStocks;

    private Map<String, List<StockStatistics>> macdStatsMap;

    private List<StockStatistics> monthStats;

    private Map<String, StockStatistics> averageStatsMap;
}
