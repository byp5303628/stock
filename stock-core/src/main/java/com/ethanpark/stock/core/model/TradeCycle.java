package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Getter
@Setter
public class TradeCycle {
    private TradeLog purchaseLog;

    private TradeLog saleLog;

    public Double getIncrease() {
        if (purchaseLog == null
                || saleLog == null) {
            return null;
        }

        if (purchaseLog.getStockBasic() == null
                || saleLog.getStockBasic() == null) {
            return null;
        }

        return saleLog.getStockBasic().getEndPrice().doubleValue() /
                purchaseLog.getStockBasic().getEndPrice().doubleValue() - 1;
    }

    public String getStartDate() {
        return purchaseLog == null ? null : purchaseLog.getStockBasic().getPartitionDate();
    }

    public String getEndDate() {
        return saleLog == null ? null : saleLog.getStockBasic().getPartitionDate();
    }

    public String getPurchasePrice() {
        return purchaseLog == null ? null : purchaseLog.getStockBasic().getEndPrice().toString();
    }

    public String getSalePrice() {
        return saleLog == null ? null : saleLog.getStockBasic().getEndPrice().toString();
    }
}
