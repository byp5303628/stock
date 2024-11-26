package com.ethanpark.stock.common.dal.general;

import com.ethanpark.stock.common.dal.mappers.entity.StockBasicDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
public interface StockBasicMapper {
    StockBasicDO selectByCodeAndPartitionDate(@Param("code") String code,
                                              @Param("partitionDate") String partitionDate);

    int updateById(StockBasicDO stockBasicDO);

    int insert(StockBasicDO stockBasicDO);

    List<StockBasicDO> selectList(@Param("code") String code, @Param("limitNum") int limit,
                                  @Param("offset") int offset);

    StockBasicDO selectLatest(@Param("code") String code);
}
