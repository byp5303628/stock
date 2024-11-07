package com.ethanpark.stock.common.dal;

import com.ethanpark.stock.common.entity.StockBasicDO;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
public interface StockBasicMapper {
    StockBasicDO selectByCodeAndPartitionDate(String code, String partitionDate);

    int updateById(StockBasicDO stockBasicDO);

    int insert(StockBasicDO stockBasicDO);
}
