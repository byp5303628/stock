package com.ethanpark.stock.core.model.indicator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Getter
@Setter
public class HistogramRect {
    private String name;
    private Integer cnt;

    private Double begin;

    private Double end;
}
