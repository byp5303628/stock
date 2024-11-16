package com.ethanpark.stock.biz.task.handler;

import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.Task;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/16
 */
@Service
public class HistoryStrategyTaskHandler extends BaseTaskHandler {
    @Override
    protected Result<Void> handle0(Task task) {

        Map<String, String> context = task.getContext();
        String code = context.get("code");



        return null;
    }
}
