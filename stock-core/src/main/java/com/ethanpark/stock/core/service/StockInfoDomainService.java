package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.StockInfoMapper;
import com.ethanpark.stock.core.model.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Service
public class StockInfoDomainService {

    @Resource
    private StockInfoMapper stockInfoMapper;


    public Result<Integer> getStockCnt() {
        Integer stockCnt = stockInfoMapper.getStockCnt();

        return Result.ok(stockCnt);
    }

    public Result<List<String>> getStockCodes() {
        return Result.ok(stockInfoMapper.getStockCodes());
    }
}
