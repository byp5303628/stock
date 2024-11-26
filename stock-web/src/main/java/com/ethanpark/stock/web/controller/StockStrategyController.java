package com.ethanpark.stock.web.controller;

import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.biz.trade.TradePolicyFactory;
import com.ethanpark.stock.web.dto.ResponseDTO;
import com.ethanpark.stock.web.dto.StrategyDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/25
 */
@RestController
@RequestMapping("/api/stock-strategy")
public class StockStrategyController {

    @Resource
    private TradePolicyFactory tradePolicyFactory;

    @GetMapping("/list.json")
    public ResponseDTO<List<StrategyDTO>> getStockStrategyList() {
        List<TradePolicy> policies = tradePolicyFactory.getPolicies();

        List<StrategyDTO> result = policies.stream().map(i -> {
            StrategyDTO strategyDTO = new StrategyDTO();
            strategyDTO.setName(i.getName());
            strategyDTO.setDescription(i.getDescription());

            return strategyDTO;
        }).collect(Collectors.toList());

        return ResponseDTO.success(result);
    }

}
