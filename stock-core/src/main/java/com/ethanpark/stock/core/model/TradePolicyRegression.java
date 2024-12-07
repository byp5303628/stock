package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class TradePolicyRegression {
    private Long id;
    private String name;

    private Long taskId;

    private RegressionDetail detail;
}
