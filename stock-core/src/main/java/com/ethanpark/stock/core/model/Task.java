package com.ethanpark.stock.core.model;

import com.ethanpark.stock.core.service.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Getter
@Setter
public class Task {
    private Long id;

    private String taskType;

    private Map<String, String> context = new HashMap<>();

    private TaskStatus status = TaskStatus.INIT;

    private Integer retryTimes = 0;

    private String errorMsg;

    private Date gmtCreate;

    private Date gmtModified;

    private Date fireTime;
}
