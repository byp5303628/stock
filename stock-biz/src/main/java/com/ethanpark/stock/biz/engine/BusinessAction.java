package com.ethanpark.stock.biz.engine;


/**
 * Author: 柏云鹏 Date: 2019/1/30.
 */
public interface BusinessAction {

    /**
     * 业务执行
     *
     * @param context
     */
    void process(ProcessContext context);
}
