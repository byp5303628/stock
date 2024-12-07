package com.ethanpark.stock.biz.engine;


/**
 * Author: 柏云鹏 Date: 2022/4/20.
 */
public interface Router {
    /**
     * 预处理, 默认流程第一步
     */
    String PRE_PROCESS = "PRE_PROCESS";

    /**
     * 处理, 第二步
     */
    String PROCESS = "PROCESS";
    /**
     * 后置处理, 第三部
     */
    String POST_PROCESS = "POST_PROCESS";

    /**
     * 错误处理
     */
    String ERROR_PROCESS = "ERROR_PROCESS";

    /**
     * 结束
     */
    String FINISH = "FINISH";

    String DEFAULT_ROUTER = "defaultRouter";

    /**
     * 路由流程, 将流程指引到新的阶段继续执行
     *
     * @param context
     */
    void route(ProcessContext context);

    void routeError(ProcessContext context);
}
