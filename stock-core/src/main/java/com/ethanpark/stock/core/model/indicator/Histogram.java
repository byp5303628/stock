package com.ethanpark.stock.core.model.indicator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/15
 */
@Getter
@Setter
public class Histogram {
    /**
     * 起始到结束时间
     */
    private String name;

    /**
     * 增长比例
     */
    private Double value = 0D;


}
