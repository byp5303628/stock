package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.StockBasicDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
public interface HfqStockBasicMapper {
    StockBasicDO selectByCodeAndPartitionDate(@Param("code") String code, @Param("partitionDate") String partitionDate);

    int updateById(StockBasicDO stockBasicDO);

    int insert(StockBasicDO stockBasicDO);
}
