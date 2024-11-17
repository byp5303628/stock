package com.ethanpark.stock.core.service;

import com.ethanpark.stock.common.dal.mappers.StockStatisticsMapper;
import com.ethanpark.stock.common.dal.mappers.entity.StockStatisticsDO;
import com.ethanpark.stock.core.converter.DbConverter;
import com.ethanpark.stock.core.converter.DomainConverter;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.StatisticsType;
import com.ethanpark.stock.core.model.StockStatistics;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, List<StockStatistics>> queryStats(String code, StatisticsType type) {
        int offset = 0;
        int limit = 100;

        List<StockStatistics> statistics = new ArrayList<>();

        while (true) {
            List<StockStatisticsDO> stockBasicDOS = stockStatisticsMapper.selectList(code,
                    type.name(), limit, offset);

            if (CollectionUtils.isEmpty(stockBasicDOS)) {
                break;
            }

            stockBasicDOS.forEach(i -> statistics.add(DomainConverter.toDomain(i)));

            if (stockBasicDOS.size() < limit) {
                break;
            }

            offset += limit;
        }

        return statistics.stream()
                .collect(Collectors.groupingBy(StockStatistics::getStatisticsName));
    }

    private Result<Void> save(StockStatistics statistic) {
        StockStatisticsDO dbEntity = DbConverter.toDbEntity(statistic);

        StockStatisticsDO existEntity =
                stockStatisticsMapper.selectByCodeAndCondition(statistic.getCode(),
                        statistic.getStatisticsName(),
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
