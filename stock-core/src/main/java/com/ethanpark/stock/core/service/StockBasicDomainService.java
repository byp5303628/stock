package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.StockBasicMapper;
import com.ethanpark.stock.common.dal.mappers.entity.StockBasicDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Service
public class StockBasicDomainService {

    @Resource
    private StockBasicMapper stockBasicMapper;

    public boolean saveStockBasic(StockBasic stockBasic) {
        StockBasicDO stockBasicDO = DbConverter.toDbEntity(stockBasic);

        StockBasicDO existEntity =
                stockBasicMapper.selectByCodeAndPartitionDate(stockBasic.getName(),
                        stockBasic.getPartitionDate());

        if (existEntity == null) {
            return stockBasicMapper.insert(stockBasicDO) > 1;
        } else {
            stockBasicDO.setId(existEntity.getId());
            return stockBasicMapper.updateById(stockBasicDO) > 0;
        }
    }
}
