package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@Getter
@Setter
public class ScheduleConfigDTO {
    private Long id;

    private String taskType;

    private String cronExpression;

    private String status;

    private Integer count;

    private String gmtCreate;

    private String gmtModified;
}
