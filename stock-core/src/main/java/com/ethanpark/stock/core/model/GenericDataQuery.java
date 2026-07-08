package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericDataQuery {
    private String market;
    private String code;
    private String modelCode;
    private String startDate;
    private String endDate;
    private int page = 1;
    private int size = 20;
}
