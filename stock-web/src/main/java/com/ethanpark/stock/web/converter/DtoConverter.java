package com.ethanpark.stock.web.converter;

import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.web.dto.StrategyDTO;

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
}
