package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import com.ethanpark.stock.biz.process.entity.aware.StockCodeListAware;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.service.StockInfoDomainService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Action("queryAllStockCodeAction")
public class QueryAllStockCodeAction implements BusinessAction {

    @Resource
    private StockInfoDomainService stockInfoDomainService;

    @Override
    public void process(ProcessContext context) {
        StockCodeListAware entity = context.getEntity();

        Result<List<String>> stockCodes = stockInfoDomainService.getStockCodes();

        if (!stockCodes.isSuccess()) {
            throw new ProcessException(ErrorCode.SYSTEM_ERROR.getCode(), context.getResultMsg());
        }

        entity.setStockCodes(stockCodes.getData());
    }
}
