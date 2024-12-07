package com.ethanpark.stock.biz.engine.impl;

import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.Router;

/**
 * Author: 柏云鹏 Date: 2022/4/20.
 */
public class DefaultRouter implements Router {
    @Override
    public void route(ProcessContext context) {
        if (FINISH.equals(context.getStage())) {
            return;
        }

        if (ERROR_PROCESS.equals(context.getStage())) {
            context.setStage(FINISH);
        } else if (POST_PROCESS.equals(context.getStage())) {
            context.setStage(FINISH);
        } else if (PROCESS.equals(context.getStage())) {
            context.setStage(POST_PROCESS);
        } else if (PRE_PROCESS.equals(context.getStage())) {
            context.setStage(PROCESS);
        } else {
            // 异常情况, 如果需要定制, 需要重写router
            context.setStage(FINISH);
        }
    }

    @Override
    public void routeError(ProcessContext context) {
        if (FINISH.equals(context.getStage())) {
            return;
        }

        if (ERROR_PROCESS.equals(context.getStage())) {
            context.setStage(FINISH);
            return;
        }

        context.setStage(ERROR_PROCESS);
    }

}
