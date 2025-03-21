package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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

    private Long externalSerialNo;

    private String taskType;

    private Map<String, String> context = new HashMap<>();

    private TaskStatus status = TaskStatus.INIT;

    private Integer retryTimes = 0;

    private String resultMsg;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private LocalDateTime fireTime;
}
