package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Schema 校验请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class ValidateRequest {
    @NotNull(message = "模型 ID 不能为空")
    private Long modelId;
}
