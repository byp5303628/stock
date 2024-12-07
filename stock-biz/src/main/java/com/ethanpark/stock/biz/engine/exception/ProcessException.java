package com.ethanpark.stock.biz.engine.exception;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public class ProcessException extends RuntimeException {
    private final Integer errorCode;
    private final String errorMsg;


    public ProcessException(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public ProcessException(Integer errorCode, String errorMsg, Throwable t) {
        super(t);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
