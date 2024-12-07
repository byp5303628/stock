package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@Getter
@Setter
public class StockRegressionDetailDO {
    private Long id;
    private String code;

    private String tradePolicyName;
    private String tradeCycles;

    private Date gmtCreate;

    private Date gmtModified;
}
