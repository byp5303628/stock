package com.ethanpark.stock.core.model;

import com.ethanpark.stock.remote.model.StockBasic;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Getter
@Setter
public class TradeContext {
    private String code;

    private int amount = 0;

    private String strategyDesc;

    private List<TradeCycle> tradeCycles = new ArrayList<>();

    public boolean hasStock() {
        return amount > 0;
    }

    public void purchase(StockBasic stockBasic) {
        if (amount == 1) {
            return;
        }

        TradeLog e = new TradeLog();
        e.setBehavior(TradeBehavior.PURCHASE);
        e.setAmount(1);
        e.setStockBasic(stockBasic);
        amount = 1;

        TradeCycle e1 = new TradeCycle();
        e1.setPurchaseLog(e);
        tradeCycles.add(e1);
    }

    public void sale(StockBasic stockBasic) {
        if (amount == 0) {
            return;
        }

        TradeLog e = new TradeLog();
        e.setBehavior(TradeBehavior.SALE);
        e.setAmount(-1);
        e.setStockBasic(stockBasic);

        amount = 0;

        tradeCycles.get(tradeCycles.size() - 1).setSaleLog(e);
    }

    public String genReport() {
        StringBuilder report = new StringBuilder();

        report.append("## 股票代码 ").append(code).append("\n");

        report.append("\n");

        report.append("策略描述: ").append(getStrategyDesc()).append("\n");
        report.append("\n");

        double increase = 1D;
        double sum = 0D;

        int addCnt = 0;

        for (TradeCycle tradeCycle : tradeCycles) {
            Double increase1 = tradeCycle.getIncrease();
            increase = increase * (1 + increase1 / 100);
            sum = sum + increase1;

            if (increase1 > 0) {
                addCnt++;
            }
        }

        report.append(String.format("收益比复合比例: %.2f%%\n", increase * 100));
        report.append(String.format("平均收益为: %.2f%%\n", sum / tradeCycles.size()));
        report.append(String.format("复合平均收益为: %.2f%%\n", increase * 100 / tradeCycles.size()));

        report.append("从2014年1月1日开始, 共计 ").append(tradeCycles.size()).append("个周期\n");
        report.append("从2014年1月1日开始, 共计 ").append(addCnt).append("个赚钱周期, 共计 ").append(tradeCycles.size() - addCnt).append("个亏损周期\n");

        if (CollectionUtils.isEmpty(tradeCycles)) {
            return report.toString();
        }

        report.append("\n");
        report.append("## 明细\n");
        report.append("\n");

        report.append("|seq|买入日期|卖出日期|盈利比|买入价|卖出价|\n");
        report.append("|---|------|--------|----|-----|-----|\n");

        for (int i = 0; i < tradeCycles.size(); i++) {
            TradeCycle tradeCycle = tradeCycles.get(i);
            report.append(String.format("|%d|%s|%s|%.2f%%|%s|%s|\n", i + 1, tradeCycle.getStartDate(), tradeCycle.getEndDate(), tradeCycle.getIncrease() * 100, tradeCycle.getPurchasePrice(), tradeCycle.getSalePrice()));
        }

        return report.toString();
    }
}
