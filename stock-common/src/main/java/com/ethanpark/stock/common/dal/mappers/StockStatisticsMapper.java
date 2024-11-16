package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.StockStatisticsDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author baiyunpeng04
 * @since 2024/11/7
 */
public interface StockStatisticsMapper {
    StockStatisticsDO selectByCodeAndCondition(@Param("code") String code, @Param("statisticsName"
    ) String statisticsName, @Param("partitionDate") String partitionDate);

    int updateById(StockStatisticsDO stockBasicDO);

    int insert(StockStatisticsDO stockBasicDO);
}
