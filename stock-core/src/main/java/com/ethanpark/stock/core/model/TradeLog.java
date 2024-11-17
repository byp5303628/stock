package com.ethanpark.stock.core.model;

import com.ethanpark.stock.remote.model.StockBasic;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Getter
@Setter
public class TradeLog {
    private String code;

    private int amount;

    private TradeBehavior behavior;

    private StockBasic stockBasic;
}
