package com.ethanpark.stock.common.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Data
public class TaskDO {
    private Long id;

    private String taskType;

    private String context;

    private String status;

    private Date gmtCreate;

    private Date gmtModified;
}
