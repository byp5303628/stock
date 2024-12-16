package com.ethanpark.stock.biz.process.actions;

import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.ethanpark.stock.biz.engine.ProcessContext;
import com.ethanpark.stock.biz.engine.entity.BaseEntity;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/17
 */
@Action("commonErrorHandleAction")
public class CommonErrorHandleAction implements BusinessAction {
    @Override
    public void process(ProcessContext context) {
        BaseEntity entity = context.getEntity();

        entity.setSuccess(false);
    }
}
