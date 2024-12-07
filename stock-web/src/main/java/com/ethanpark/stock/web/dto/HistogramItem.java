package com.ethanpark.stock.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class HistogramItem {
    private String name;

    private Integer cnt;

    private String rate;
}
