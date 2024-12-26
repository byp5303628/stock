package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.converter.DtoConverter;
import com.ethanpark.stock.biz.dto.StockPredictIndicatorDTO;
import com.ethanpark.stock.biz.dto.StockRegressionDetailDTO;
import com.ethanpark.stock.biz.dto.TradeCycleDTO;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.process.entity.StockStrategyDetailEntity;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.model.TradeCycle;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/17
 */
@Action("buildStockRegressionDetailRespAction")
public class BuildStockRegressionDetailRespAction implements BusinessAction {

    @Override
    public void process(ProcessContext context) {
        StockStrategyDetailEntity entity = context.getEntity();

        StockRegressionDetail stockRegressionDetail = entity.getStockRegressionDetail();

        if (stockRegressionDetail == null) {
            return;
        }

        StockRegressionDetailDTO resultDTO = new StockRegressionDetailDTO();

        resultDTO.setCode(entity.getCode());

        StockPredictIndicatorDTO stockPredictIndicator = new StockPredictIndicatorDTO();

        BeanUtils.copyProperties(entity.getStockRegressionDetail().getStockPredictIndicator(), stockPredictIndicator);

        resultDTO.setStockPredictIndicator(stockPredictIndicator);
        List<TradeCycle> tradeCycles = stockRegressionDetail.getTradeCycles();


        List<TradeCycleDTO> tradeCycleDtos = tradeCycles.stream().map(i -> DtoConverter.toDto(i)).collect(Collectors.toList());

        resultDTO.setTradeCycles(tradeCycleDtos);
        resultDTO.setStrategyDetailDTO(entity.getStrategyDetailDTO());

        entity.setResultDTO(resultDTO);
    }
}
