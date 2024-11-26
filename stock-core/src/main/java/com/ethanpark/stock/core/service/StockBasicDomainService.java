package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.HfqStockBasicMapper;
import com.ethanpark.stock.common.dal.mappers.QfqStockBasicMapper;
import com.ethanpark.stock.common.dal.mappers.entity.StockBasicDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.remote.model.StockBasic;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    public List<StockBasic> queryAllHfqStockBasics(String code) {
        int offset = 0;
        int limit = 100;

        List<StockBasic> stockBasics = new ArrayList<>();

        while (true) {
            List<StockBasicDO> stockBasicDOS = hfqStockBasicMapper.selectList(code, limit, offset);

            if (CollectionUtils.isEmpty(stockBasicDOS)) {
                break;
            }

            stockBasicDOS.forEach(i -> stockBasics.add(DomainConverter.toDomain(i)));

            if (stockBasicDOS.size() < 100) {
                break;
            }

            offset += limit;
        }

        return stockBasics;
    }

    public StockBasic getLatestStockBasic(String code) {
        return null;
    }
}
