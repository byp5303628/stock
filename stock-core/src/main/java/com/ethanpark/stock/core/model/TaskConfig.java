package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Getter
@Setter
public class TaskConfig {

    private String taskType;

    private String cronExpression;

    private int limit;
}
