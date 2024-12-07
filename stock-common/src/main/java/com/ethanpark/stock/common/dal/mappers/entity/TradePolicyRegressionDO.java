package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class TradePolicyRegressionDO {
    private Long id;

    private String name;

    private Long taskId;

    private String detail;

    private Date gmtCreate;

    private Date gmtModified;
}
