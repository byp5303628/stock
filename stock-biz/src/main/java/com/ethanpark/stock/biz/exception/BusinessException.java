package com.ethanpark.stock.biz.exception;

import com.ethanpark.stock.biz.ErrorCode;

/**
 * 业务异常 — 被 GlobalExceptionHandler 统一捕获并转为 ResponseDTO.
 *
 * <p>Controller 或 Service 层遇到业务错误时抛出此异常,
 * 替代手动调用 {@code ResponseDTO.error()} 返回.
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
