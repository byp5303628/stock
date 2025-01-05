package com.ethanpark.stock.biz.cal.factory;

import com.ethanpark.stock.core.model.StockRegressionDetail;
import com.ethanpark.stock.core.model.indicator.Indicator;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
public interface StrategyIndicatorCalculator {

    Indicator calculate(List<StockRegressionDetail> details);

    default String getName() {
        String s = this.getClass().getSimpleName();

        return s.substring(0, s.indexOf("Calculator"));
    }

    String getDescription();

    default Indicator buildNew() {
        Indicator indicator = new Indicator();

        indicator.setName(getName());
        indicator.setDescription(getDescription());
        indicator.setType(getType());

        return indicator;
    }

    String getType();
}
