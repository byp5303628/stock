package com.ethanpark.stock.biz.dto;

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

    public static <T> ResponseDTO<T> success() {
        ResponseDTO<T> responseDTO = new ResponseDTO<>();

        return responseDTO;
    }

    public static <T> ResponseDTO<T> success(T data) {
        ResponseDTO<T> responseDTO = new ResponseDTO<>();
        responseDTO.setData(data);

        return responseDTO;
    }

    public static <T> ResponseDTO<T> error(int code, String msg) {
        ResponseDTO<T> responseDTO = new ResponseDTO<>();
        responseDTO.setCode(code);
        responseDTO.setMsg(msg);

        return responseDTO;
    }
}
