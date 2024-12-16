package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.StockRegressionDetailMapper;
import com.ethanpark.stock.common.dal.mappers.entity.StockRegressionDetailDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.model.indicator.StockPredictIndicator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@Service
public class StockRegressionDomainService {

    @Resource
    private StockRegressionDetailMapper stockRegressionDetailMapper;

    @Resource
    private IndicatorDomainService indicatorDomainService;

    public Result<Void> saveRegression(StockRegressionDetail stockRegressionDetail) {
        StockRegressionDetailDO dbEntity = DbConverter.toDbEntity(stockRegressionDetail);

        return stockRegressionDetailMapper.save(dbEntity) > 0 ? Result.ok() : Result.fail("保存失败!");
    }

    public List<StockRegressionDetail> getRegressionDetails(String policy) {
        List<StockRegressionDetailDO> stockRegressionDetailDOS =
                stockRegressionDetailMapper.selectByPolicy(policy);

        List<StockRegressionDetail> list = stockRegressionDetailDOS.stream().map(DomainConverter::toDomain).collect(Collectors.toList());

        list.forEach(i -> {
            StockPredictIndicator calculate = indicatorDomainService.calculate(i.getTradeCycles());

            i.setStockPredictIndicator(calculate);
        });

        return list;
    }

    public StockRegressionDetail getRegressionDetailByCodeAndPolicy(String code, String policy) {
        StockRegressionDetailDO stockRegressionDetailDO = stockRegressionDetailMapper.selectByCodeAndPolicy(code, policy);

        StockRegressionDetail domain = DomainConverter.toDomain(stockRegressionDetailDO);

        StockPredictIndicator calculate = indicatorDomainService.calculate(domain.getTradeCycles());

        domain.setStockPredictIndicator(calculate);

        return domain;
    }
}
