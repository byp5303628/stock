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
    private int code = 200;

    private String msg = "成功";

    private T data;

    public static <T> ResponseDTO<T> success(T data) {
        ResponseDTO<T> responseDTO = new ResponseDTO<>();
        responseDTO.setData(data);

        return responseDTO;
    }
}
