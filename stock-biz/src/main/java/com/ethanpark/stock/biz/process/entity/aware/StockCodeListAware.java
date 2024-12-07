package com.ethanpark.stock.biz.process.entity.aware;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public interface StockCodeListAware {
    List<String> getStockCodes();

    void setStockCodes(List<String> codes);
}
