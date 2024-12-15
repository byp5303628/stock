package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Getter
@Setter
public class ScheduleConfig {
    private Long id;

    private String taskType;

    private String cronExpression;

    private int count;

    private String status;

    private Date gmtCreate;

    private Date gmtModified;


}
