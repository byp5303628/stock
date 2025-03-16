package com.ethanpark.stock.common.dal.mappers.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Data
public class TaskDO {
    private Long id;

    private Long externalSerialNo;

    private String taskType;

    private String context;

    private String status;

    private Integer retryTimes;

    private String resultMsg;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private LocalDateTime fireTime;
}
