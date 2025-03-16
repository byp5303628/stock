package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.cal.factory.IndicatorFactory;
import com.ethanpark.stock.biz.cal.factory.strategyindicators.StrategyIndicatorCalculator;
import com.ethanpark.stock.biz.dto.StockPredictIndicatorDTO;
import com.ethanpark.stock.biz.dto.StrategyDetailDTO;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.process.entity.StrategyDetailEntity;
import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.model.indicator.Indicator;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Action("buildStrategyIndicatorRespAction")
public class BuildStrategyIndicatorRespAction implements BusinessAction {

    @Resource
    private IndicatorFactory indicatorFactory;

    @Override
    public void process(ProcessContext context) {
        StrategyDetailEntity entity = context.getEntity();

        List<StockRegressionDetail> stockRegressionDetails = entity.getStockRegressionDetails();

        List<Indicator> indicators = new ArrayList<>();

        List<StrategyIndicatorCalculator> calculators = indicatorFactory.selectStrategyCalculators();

        List<StockPredictIndicatorDTO> indicatorDTOS = new ArrayList<>();

        for (StockRegressionDetail stockRegressionDetail : stockRegressionDetails) {
            StockPredictIndicatorDTO stockPredictIndicator = new StockPredictIndicatorDTO();

            if (stockRegressionDetail.getStockPredictIndicator() == null) {
                continue;
            }

            BeanUtils.copyProperties(stockRegressionDetail.getStockPredictIndicator(), stockPredictIndicator);

            indicatorDTOS.add(stockPredictIndicator);
        }


        for (StrategyIndicatorCalculator calculator : calculators) {
            Indicator indicator = calculator.calculate(stockRegressionDetails);

            if (indicator == null) {
                continue;
            }

            indicators.add(indicator);
        }

        StrategyDetailDTO strategyDetailDTO = entity.getStrategyDetailDTO();

        strategyDetailDTO.setStockPredictIndicators(indicatorDTOS);
        strategyDetailDTO.setIndicators(indicators);
    }
}
