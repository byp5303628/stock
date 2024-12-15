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
public class ScheduleConfigDO {
    private Long id;

    private String taskType;

    private String cronExpression;

    private String status;

    private Integer count;

    private Date gmtCreate;

    private Date gmtModified;
}
