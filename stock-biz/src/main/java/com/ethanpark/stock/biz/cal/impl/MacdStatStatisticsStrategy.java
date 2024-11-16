package com.ethanpark.stock.biz.cal.impl;

import com.ethanpark.stock.biz.cal.StatisticsStrategy;
import com.ethanpark.stock.common.util.MathUtils;
import com.ethanpark.stock.core.model.MacdStat;
import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockStatistics;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/16
 */
public class MacdStatStatisticsStrategy implements StatisticsStrategy {

    private int start = 12;

    private int end = 26;

    private int dea = 9;

    public MacdStatStatisticsStrategy() {
    }

    public MacdStatStatisticsStrategy(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public List<StockStatistics> calculate(List<StockBasic> stockBasicList) {
        List<BigDecimal> endPriceList =
                stockBasicList.stream().map(i -> i.getEndPrice()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(endPriceList)) {
            return Collections.emptyList();
        }

        List<MacdStat> macdStats = calculateMacd(endPriceList);

        List<StockStatistics> statistics = new ArrayList<>();

        for (int i = 0; i < stockBasicList.size(); i++) {
            StockBasic stockBasic = stockBasicList.get(i);
            MacdStat macdStat = macdStats.get(i);

            StockStatistics stockStatistics = new StockStatistics();

            stockStatistics.setCode(stockBasic.getCode());
            stockStatistics.setStatisticsType(getStatisticsType());
            stockStatistics.setPartitionDate(stockBasic.getPartitionDate());
            stockStatistics.setStatisticsName(getName());
            stockStatistics.getStatistics().put("start", new BigDecimal(macdStat.getStart()));
            stockStatistics.getStatistics().put("end", new BigDecimal(macdStat.getEnd()));
            stockStatistics.getStatistics().put("deaLimit", new BigDecimal(macdStat.getDeaLimit()));

            stockStatistics.getStatistics().put("diff", macdStat.getDiff());
            stockStatistics.getStatistics().put("emaStart", macdStat.getEmaStart());
            stockStatistics.getStatistics().put("emaEnd", macdStat.getEmaEnd());
            stockStatistics.getStatistics().put("dea", macdStat.getDea());
            stockStatistics.getStatistics().put("macd", macdStat.getMacd());

            statistics.add(stockStatistics);
        }

        return statistics;
    }

    public List<MacdStat> calculateMacd(List<BigDecimal> numbers) {
        List<BigDecimal> startList = MathUtils.calEma(start, numbers);

        List<BigDecimal> endList = MathUtils.calEma(end, numbers);

        List<MacdStat> macdStats = new ArrayList<>(numbers.size());

        List<BigDecimal> diffs = new ArrayList<>();

        for (int i = 0; i < numbers.size(); i++) {
            MacdStat macdStat = new MacdStat();
            macdStat.setStart(start);
            macdStat.setEnd(end);
            macdStat.setDeaLimit(dea);
            macdStat.setEmaStart(startList.get(i));
            macdStat.setEmaEnd(endList.get(i));
            macdStat.setDiff(macdStat.getEmaStart().subtract(macdStat.getEmaEnd()));

            diffs.add(macdStat.getDiff());

            macdStats.add(macdStat);
        }

        List<BigDecimal> bigDecimals = MathUtils.calEma(dea, diffs);

        for (int i = 0; i < macdStats.size(); i++) {
            MacdStat macdStat = macdStats.get(i);
            macdStat.setDea(bigDecimals.get(i));
            macdStat.setMacd(macdStat.getDiff().subtract(macdStat.getDea()).multiply(new BigDecimal(2)));
        }

        return macdStats;
    }


    @Override
    public StatisticsType getStatisticsType() {
        return StatisticsType.MACD;
    }

    @Override
    public String getName() {
        return String.format("%s_%d_%d", getStatisticsType().name(), start, end);
    }
}
