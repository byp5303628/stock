package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.dto.StrategyDetailDTO;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.process.entity.StrategyDetailEntity;
import com.ethanpark.stock.biz.trade.TradePolicy;

import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Action("calculatePolicyDetailAction")
public class CalculatePolicyDetailAction implements BusinessAction {
    @Override
    public void process(ProcessContext context) {
        StrategyDetailEntity entity = context.getEntity();

        StrategyDetailDTO detailDTO = new StrategyDetailDTO();

        TradePolicy tradePolicy = entity.getTradePolicy();
        detailDTO.setName(tradePolicy.getName());
        detailDTO.setDescription(tradePolicy.getDescription());
        detailDTO.setTags(tradePolicy.getStatisticsTypes().stream()
                .map(Enum::name).collect(Collectors.toList()));

        detailDTO.setTotalStockCnt(entity.getStockCnt());

        entity.setStrategyDetailDTO(detailDTO);
    }
}
