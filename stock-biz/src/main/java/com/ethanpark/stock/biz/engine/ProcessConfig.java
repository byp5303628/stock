package com.ethanpark.stock.biz.engine;

import java.util.List;

/**
 * Author: 柏云鹏
 * Date: 2022/4/20.
 */
public interface ProcessConfig {

    /**
     * 放回当前阶段对应的流程
     * @return
     */
    List<String> getActions(String stage);

    /**
     * 返回当前阶段对应的路由配置
     * @return
     */
    String getRouter(String stage);
}
