package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import com.ethanpark.stock.biz.process.entity.aware.StockCntAware;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.service.StockInfoDomainService;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Action("getStockCntAction")
public class GetStockCntAction implements BusinessAction {

    @Resource
    private StockInfoDomainService stockInfoDomainService;

    @Override
    public void process(ProcessContext context) {
        StockCntAware entity = context.getEntity();

        Result<Integer> stockCnt = stockInfoDomainService.getStockCnt();

        if (!stockCnt.isSuccess()) {
            throw new ProcessException(ErrorCode.SYSTEM_ERROR.getCode(), stockCnt.getMsg());
        }

        entity.setStockCnt(stockCnt.getData());
    }
}
