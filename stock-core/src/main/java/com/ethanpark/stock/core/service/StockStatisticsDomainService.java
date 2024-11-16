package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.StockStatisticsMapper;
import com.ethanpark.stock.common.dal.mappers.entity.StockStatisticsDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.StockStatistics;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/17
 */
@Service
public class StockStatisticsDomainService {

    @Resource
    private StockStatisticsMapper stockStatisticsMapper;

    public Result<Void> batchSave(List<StockStatistics> statistics) {
        if (CollectionUtils.isEmpty(statistics)) {
            return Result.ok();
        }

        for (StockStatistics statistic : statistics) {
            Result<Void> result = save(statistic);

            if (!result.isSuccess()) {
                return result;
            }
        }

        return Result.ok();
    }

    public Result<Void> save(StockStatistics statistic) {
        StockStatisticsDO dbEntity = DbConverter.toDbEntity(statistic);

        StockStatisticsDO existEntity =
                stockStatisticsMapper.selectByCodeAndCondition(statistic.getCode(), statistic.getStatisticsName(),
                        statistic.getPartitionDate());

        if (existEntity == null) {
            if (stockStatisticsMapper.insert(dbEntity) > 0) {
                return Result.ok();
            }
        } else {
            dbEntity.setId(existEntity.getId());
            if (stockStatisticsMapper.updateById(dbEntity) > 0) {
                return Result.ok();
            }
        }

        return Result.fail("保存失败!");
    }
}
