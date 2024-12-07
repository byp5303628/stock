package com.ethanpark.stock.common.dal.mappers;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public interface StockInfoMapper {
    List<String> getStockCodes();

    Integer getStockCnt();
}
