package com.ethanpark.stock.biz.engine.impl;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.*;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: 柏云鹏 Date: 2022/4/20.
 */
@Service
public class ProcessExecutorImpl implements ProcessExecutor {
    private static final Router DEFAULT_ROUTER = new DefaultRouter();

    private static final Logger log = LoggerFactory.getLogger("PROCESS-ENGINE");

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public void execute(ProcessContext context) {
        if (log.isDebugEnabled()) {
            log.debug("[ProcessExecutor] start to process, context=" + JSON.toJSONString(context));
        }
        if (Router.FINISH.equals(context.getStage())) {
            String detail = String.format("%s_%s_%d_%s", context.getProductCode(),
                    context.getBusinessCode(), context.getResultCode(), context.getResultMsg());
            return;
        }

        Router router = getRouter(context);

        String currentAction = null;
        String currentStage = context.getStage();

        try {
            ProcessConfig processConfig = context.getProcessConfig();

            if (processConfig == null) {
                String msg = String.format("流程未找到! productCode=%s, businessCode=%s",
                        context.getProductCode(), context.getBusinessCode());
                throw new ProcessException(ErrorCode.ILLEGAL_PARAM.getCode(), "编排流程引擎异常: " + msg);
            }

            List<String> actions = processConfig.getActions(context.getStage());

            for (String action : actions) {
                currentAction = action;
                BusinessAction a = applicationContext.getBean(action, BusinessAction.class);
                log.info("[ProcessExecutor] processing, action={}", action);
                a.process(context);

                if (context.isBreakToFinish()) {
                    context.setStage(Router.FINISH);
                    return;
                }
            }
            router.route(context);
        } catch (ProcessException e) {

            context.setResultCode(e.getErrorCode());
            context.setResultMsg(e.getErrorMsg());
            log.warn("[ProcessExecutor] process internal error! stage={}, errorAction={}, " +
                            "context={}, errorCode={}, errorMsg={}", context.getStage(),
                    currentAction, JSON.toJSONString(context),
                    e.getErrorCode(), e.getErrorMsg(), e);
            router.routeError(context);
        } catch (Exception e) {
            String detail = String.format("%s_%s_%s", context.getProductCode(),
                    context.getBusinessCode(), e.getMessage());
            context.setResultCode(ErrorCode.SYSTEM_ERROR.getCode());
            context.setResultMsg(ErrorCode.SYSTEM_ERROR.getMsg());
            log.error("[ProcessExecutor] process error! stage={}, errorAction={}, context={}",
                    context.getStage(), currentAction, JSON.toJSONString(context),
                    e);
            router.routeError(context);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug(String.format("[ProcessExecutor] process turn from [%s] to [%s], " +
                                "router=%s",
                        currentStage, context.getStage(), router.getClass().getSimpleName()));
            }
        }

        execute(context);
    }

    private Router getRouter(ProcessContext context) {
        ProcessConfig config = context.getProcessConfig();

        if (config == null) {
            return DEFAULT_ROUTER;
        }

        String routerBean = config.getRouter(context.getStage());

        if (StringUtils.isEmpty(routerBean)) {
            return DEFAULT_ROUTER;
        }

        try {
            return applicationContext.getBean(routerBean, Router.class);
        } catch (Exception e) {
            return DEFAULT_ROUTER;
        }
    }
}
