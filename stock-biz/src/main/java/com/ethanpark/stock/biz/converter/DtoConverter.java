package com.ethanpark.stock.biz.converter;

import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.common.util.DateUtils;
import com.ethanpark.stock.core.model.ScheduleConfig;
import com.ethanpark.stock.biz.dto.ScheduleConfigDTO;
import com.ethanpark.stock.biz.dto.StrategyDTO;
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
}
