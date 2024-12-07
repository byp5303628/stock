package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.TradePolicyRegressionMapper;
import com.ethanpark.stock.common.dal.mappers.entity.TradePolicyRegressionDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.TradePolicyRegression;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Service
public class TradePolicyRegressionDomainService {

    @Resource
    private TradePolicyRegressionMapper tradePolicyRegressionMapper;

    public TradePolicyRegression loadByName(String name) {
        TradePolicyRegressionDO tradePolicyRegressionDO =
                tradePolicyRegressionMapper.selectByName(name);

        return DomainConverter.toDomain(tradePolicyRegressionDO);
    }

    public Result<Void> saveRegression(TradePolicyRegression regression) {
        TradePolicyRegressionDO tradePolicyRegressionDO =
                tradePolicyRegressionMapper.selectByName(regression.getName());

        TradePolicyRegressionDO origin = DbConverter.toDbEntity(regression);

        if (tradePolicyRegressionDO != null) {
            origin.setId(tradePolicyRegressionDO.getId());

            return tradePolicyRegressionMapper.updateById(origin) > 0 ? Result.ok() : Result.fail("更新失败!");
        } else {
            return tradePolicyRegressionMapper.insert(origin) > 0 ? Result.ok() : Result.fail("新增失败!");
        }
    }
}
