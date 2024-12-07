package com.ethanpark.stock.web.controller;

import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.ProcessExecutor;
import com.ethanpark.stock.biz.engine.config.ProcessConfigCache;
import com.ethanpark.stock.biz.engine.config.ProcessConfigCacheImpl;
import com.ethanpark.stock.biz.process.entity.StrategyDetailEntity;
import com.ethanpark.stock.biz.process.entity.StrategyDetailRegressionEntity;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.biz.trade.TradePolicyFactory;
import com.ethanpark.stock.web.converter.DtoConverter;
import com.ethanpark.stock.web.dto.ResponseDTO;
import com.ethanpark.stock.web.dto.StrategyDTO;
import com.ethanpark.stock.web.dto.StrategyDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Resource
    private ProcessConfigCache processConfigCache;

    @Resource
    private ProcessExecutor processExecutor;
    @Autowired
    private ProcessConfigCacheImpl processConfigCacheImpl;

    @GetMapping("/list.json")
    public ResponseDTO<List<StrategyDTO>> getStockStrategyList() {
        List<TradePolicy> policies = tradePolicyFactory.getPolicies();

        List<StrategyDTO> result = policies.stream()
                .map(DtoConverter::toDto).collect(Collectors.toList());

        return ResponseDTO.success(result);
    }

    @GetMapping("/detail.json")
    public ResponseDTO<StrategyDetailDTO> getStockStrategyByName(String name) {
        ProcessContext context = new ProcessContext();

        context.setProductCode("stock_strategy");
        context.setBusinessCode("detail");

        StrategyDetailEntity entity = new StrategyDetailEntity();

        context.setEntity(entity);

        entity.setName(name);

        context.setProcessConfig(processConfigCache.getProcessConfig(context));

        processExecutor.execute(context);

        if (entity.isSuccess()) {
            StrategyDetailDTO detailDTO = new StrategyDetailDTO();

            TradePolicy tradePolicy = entity.getTradePolicy();
            detailDTO.setName(tradePolicy.getName());
            detailDTO.setDescription(tradePolicy.getDescription());
            detailDTO.setTags(tradePolicy.getStatisticsTypes().stream()
                    .map(Enum::name).collect(Collectors.toList()));

            detailDTO.setTotalStockCnt(entity.getStockCnt());

            return ResponseDTO.success(detailDTO);
        } else {
            return ResponseDTO.error(context.getResultCode(), context.getResultMsg());
        }
    }

    @PostMapping("/create-regression.json")
    public ResponseDTO<Void> createRegression(String name) {
        ProcessContext context = new ProcessContext();

        context.setProductCode("stock_strategy");
        context.setBusinessCode("create_regression");

        StrategyDetailRegressionEntity entity = new StrategyDetailRegressionEntity();

        context.setEntity(entity);

        entity.setName(name);

        context.setProcessConfig(processConfigCache.getProcessConfig(context));

        processExecutor.execute(context);

        if (entity.isSuccess()) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.error(context.getResultCode(), context.getResultMsg());
        }

    }

}
