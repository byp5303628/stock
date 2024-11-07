package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/7
 */
@Setter
@Getter
public class Result<V> {
    private boolean success = true;
    private String msg = "成功";
    private V data;

    public static <V> Result<V> ok() {
        Result<V> result = new Result<>();
        return result;
    }

    public static <V> Result<V> ok(V value) {
        Result<V> res = new Result<>();
        res.setData(value);

        return res;
    }

    public static <V> Result<V> fail(String msg) {
        Result<V> result = new Result<>();

        result.setSuccess(false);
        result.setMsg(msg);
        return result;
    }
}
