package com.ethanpark.stock.biz.task.handler;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.biz.trade.TradePolicy;
import com.ethanpark.stock.biz.trade.TradePolicyFactory;
import com.ethanpark.stock.core.model.*;
import com.ethanpark.stock.core.service.StockRegressionDomainService;
import com.ethanpark.stock.core.service.TradePolicyRegressionDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Slf4j
@Service
public class TradePolicyRegressionTaskHandler extends BaseTaskHandler {

    @Resource
    private TradePolicyRegressionDomainService tradePolicyRegressionDomainService;

    @Resource
    private TradePolicyFactory tradePolicyFactory;

    @Resource
    private StockRegressionDomainService stockRegressionDomainService;

    @Override
    public void handle(Task task) {
        try {
            Map<String, String> context = task.getContext();

            String tradePolicyName = context.get("name");

            TradePolicyRegression regression =
                    tradePolicyRegressionDomainService.loadByName(tradePolicyName);

            if (regression == null) {
                dealFail(task, "无法查到对应的回溯任务");
                return;
            }
            TradePolicy tradePolicy = tradePolicyFactory.getTradePolicy(tradePolicyName);

            if (tradePolicy == null) {
                dealFail(task, "无法查到对应的交易策略!");
                return;
            }

            RegressionDetail detail = regression.getDetail();

            List<String> unFinishCodes = detail.getUnFinishCodes();

            if (unFinishCodes.isEmpty()) {
                dealSuccess(task);
                return;
            }

            int cnt = 0;

            for (String code : unFinishCodes) {
                cnt++;

                if (cnt > 20) {
                    break;
                }

                TradeContext ctx = tradePolicy.trade(code);

                StockRegressionDetail stockRegressionDetail = new StockRegressionDetail();

                stockRegressionDetail.setCode(code);
                stockRegressionDetail.setTradeCycles(ctx.getTradeCycles());
                stockRegressionDetail.setTradePolicyName(tradePolicyName);

                Result<Void> result =
                        stockRegressionDomainService.saveRegression(stockRegressionDetail);

                if (!result.isSuccess()) {
                    dealFail(task, result.getMsg());
                    break;
                }

                regression.getDetail().getSuccessCodes().add(code);
            }

            tradePolicyRegressionDomainService.saveRegression(regression);

            if (regression.getDetail().isFinished()) {
                dealSuccess(task);
            } else {
                dealRetry(task);
            }

        } catch (Exception e) {
            log.error("执行任务失败! task={}", JSON.toJSONString(task), e);
            dealFail(task, e.getMessage());
        }
    }

    private void dealRetry(Task task) {
        task.setStatus(TaskStatus.RETRY);
        task.setResultMsg("尚未完成, 需要继续执行");
        taskDomainService.save(task);
    }

    @Override
    protected Result<Void> handle0(Task task) {
        return null;
    }
}
