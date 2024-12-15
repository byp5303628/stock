package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.StockRegressionDetailDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
public interface StockRegressionDetailMapper {
    int save(StockRegressionDetailDO dbEntity);

    List<StockRegressionDetailDO> selectByPolicy(@Param("policy") String policy);

    StockRegressionDetailDO selectByCodeAndPolicy(@Param("code") String code,
                                                  @Param("policy") String policy);
}
