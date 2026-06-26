package com.ethanpark.stock.biz.handler;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理器 — 统一捕获异常并转为 ResponseDTO.
 *
 * <p>所有 Controller 抛出的异常从此处收敛，Controller 层不再需要手动 try-catch.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseDTO<Void> handleBusiness(BusinessException e) {
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseDTO<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseDTO.error(ErrorCode.ILLEGAL_PARAM.getCode(), "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseDTO<Void> handleUnknown(Exception e) {
        log.error("未处理的异常", e);
        return ResponseDTO.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部错误，请稍后重试");
    }
}
