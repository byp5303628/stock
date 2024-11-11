package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.HfqStockBasicMapper;
import com.ethanpark.stock.common.dal.mappers.QfqStockBasicMapper;
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
    private QfqStockBasicMapper qfqStockBasicMapper;

    @Resource
    private HfqStockBasicMapper hfqStockBasicMapper;

    public boolean saveQfqStockBasic(StockBasic stockBasic) {
        StockBasicDO stockBasicDO = DbConverter.toDbEntity(stockBasic);

        StockBasicDO existEntity =
                qfqStockBasicMapper.selectByCodeAndPartitionDate(stockBasic.getCode(),
                        stockBasic.getPartitionDate());

        if (existEntity == null) {
            return qfqStockBasicMapper.insert(stockBasicDO) > 0;
        } else {
            stockBasicDO.setId(existEntity.getId());
            return qfqStockBasicMapper.updateById(stockBasicDO) > 0;
        }
    }

    public boolean saveHfqStockBasic(StockBasic stockBasic) {
        StockBasicDO stockBasicDO = DbConverter.toDbEntity(stockBasic);

        StockBasicDO existEntity =
                hfqStockBasicMapper.selectByCodeAndPartitionDate(stockBasic.getCode(),
                        stockBasic.getPartitionDate());

        if (existEntity == null) {
            return hfqStockBasicMapper.insert(stockBasicDO) > 0;
        } else {
            stockBasicDO.setId(existEntity.getId());
            return hfqStockBasicMapper.updateById(stockBasicDO) > 0;
        }
    }
}
