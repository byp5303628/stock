package com.ethanpark.stock.biz;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public enum ErrorCode {
    SUCCESS(200, "成功"),
    ILLEGAL_PARAM(202, "参数异常"),
    SYSTEM_ERROR(208, "系统异常"),
    ;
    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
