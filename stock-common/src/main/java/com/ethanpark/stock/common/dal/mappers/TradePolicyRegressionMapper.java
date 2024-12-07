package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.TradePolicyRegressionDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public interface TradePolicyRegressionMapper {
    TradePolicyRegressionDO selectByName(@Param("name") String name);

    int deleteByName(@Param("name") String name);

    int insert(TradePolicyRegressionDO dbEntity);

    int updateById(TradePolicyRegressionDO dbEntity);
}
