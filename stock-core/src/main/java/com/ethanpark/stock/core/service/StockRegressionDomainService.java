package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.StockRegressionDetailMapper;
import com.ethanpark.stock.common.dal.mappers.entity.StockRegressionDetailDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@Service
public class StockRegressionDomainService {

    @Resource
    private StockRegressionDetailMapper stockRegressionDetailMapper;

    public Result<Void> saveRegression(StockRegressionDetail stockRegressionDetail) {
        StockRegressionDetailDO dbEntity = DbConverter.toDbEntity(stockRegressionDetail);

        return stockRegressionDetailMapper.save(dbEntity) > 0 ? Result.ok() : Result.fail("保存失败!");
    }
}
