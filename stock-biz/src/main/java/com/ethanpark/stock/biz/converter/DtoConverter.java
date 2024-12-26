package com.ethanpark.stock.biz.converter;

import com.ethanpark.stock.biz.dto.ScheduleConfigDTO;
import com.ethanpark.stock.biz.dto.StrategyDTO;
import com.ethanpark.stock.biz.dto.TradeCycleDTO;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.common.util.DateUtils;
import com.ethanpark.stock.core.model.ScheduleConfig;
import com.ethanpark.stock.core.model.TradeCycle;
import org.springframework.beans.BeanUtils;

import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/2
 */
public class DtoConverter {
    public static StrategyDTO toDto(TradePolicy domain) {
        StrategyDTO strategyDTO = new StrategyDTO();
        strategyDTO.setName(domain.getName());
        strategyDTO.setDescription(domain.getDescription());
        strategyDTO.setTags(domain.getStatisticsTypes().stream()
                .map(Enum::name).collect(Collectors.toList()));

        return strategyDTO;
    }

    public static ScheduleConfigDTO toDto(ScheduleConfig scheduleConfig) {

        ScheduleConfigDTO result = new ScheduleConfigDTO();

        BeanUtils.copyProperties(scheduleConfig, result);

        result.setGmtCreate(DateUtils.formatDate(scheduleConfig.getGmtCreate()));
        result.setGmtModified(DateUtils.formatDate(scheduleConfig.getGmtModified()));

        return result;
    }

    public static ScheduleConfig toDomain(ScheduleConfigDTO scheduleConfigDTO) {
        ScheduleConfig result = new ScheduleConfig();

        BeanUtils.copyProperties(scheduleConfigDTO, result);

        return result;
    }

    public static TradeCycleDTO toDto(TradeCycle tradeCycle) {
        TradeCycleDTO tradeCycleDTO = new TradeCycleDTO();

        tradeCycleDTO.setIncrease(tradeCycle.getIncrease());
        tradeCycleDTO.setPurchaseDate(tradeCycle.getStartDate());
        tradeCycleDTO.setSaleDate(tradeCycle.getEndDate());
        tradeCycleDTO.setPurchasePrice(tradeCycle.getPurchasePrice());
        tradeCycleDTO.setSalePrice(tradeCycle.getSalePrice());
        tradeCycleDTO.setPurchaseDetail(tradeCycle.getPurchaseLog().getStockBasic());
        tradeCycleDTO.setSaleDetail(tradeCycle.getSaleLog().getStockBasic());
        tradeCycleDTO.setGoldDays(DateUtils.dayDiff(tradeCycle.getStartDate(), tradeCycle.getEndDate()));

        return tradeCycleDTO;
    }
}
