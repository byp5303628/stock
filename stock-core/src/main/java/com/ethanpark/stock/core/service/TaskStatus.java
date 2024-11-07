package com.ethanpark.stock.core.service;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
public enum TaskStatus {
    INIT,
    RETRY,
    SUCCESS,
    PROCESSING,
    FAIL
}