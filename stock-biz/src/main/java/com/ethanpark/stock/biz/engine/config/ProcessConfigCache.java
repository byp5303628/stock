package com.ethanpark.stock.biz.engine.config;


import com.ethanpark.stock.biz.engine.ProcessConfig;
import com.ethanpark.stock.biz.engine.ProcessContext;

/**
 * 执行流程缓存, 用户可自定义进行扩展
 * Author: 柏云鹏 Date: 2022/4/20.
 */
public interface ProcessConfigCache {
    /**
     * 根据上下文获取对应的执行阶段
     *
     * @param context
     * @return
     */
    ProcessConfig getProcessConfig(ProcessContext context);
}
