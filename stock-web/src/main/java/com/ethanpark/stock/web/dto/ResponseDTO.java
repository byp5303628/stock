package com.ethanpark.stock.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/25
 */
@Getter
@Setter
public class ResponseDTO<T> {
    private int code;

    private String msg;

    private T data;
}
