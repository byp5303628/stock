package com.ethanpark.stock.core.model.indicator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2025/1/4
 */
@Setter
@Getter
public class Indicator {
    private String name;

    private String description;

    private Number value;

    private String type;
}
