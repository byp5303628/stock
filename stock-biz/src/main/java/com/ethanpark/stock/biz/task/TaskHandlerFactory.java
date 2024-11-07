package com.ethanpark.stock.biz.task;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Service
public class TaskHandlerFactory {

    @Resource
    private List<TaskHandler> handlers;

    private Map<String, TaskHandler> handlerMap;

    @PostConstruct
    private void init() {
        handlerMap = handlers.stream().collect(Collectors.toMap(TaskHandler::getTaskType,
                Function.identity()));
    }

    public TaskHandler selectHandler(String taskType) {
        return handlerMap.get(taskType);
    }
}
