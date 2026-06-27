package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 枚举解绑请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class EnumUnbindRequest {
    @NotNull(message = "字段 ID 不能为空")
    private Long fieldId;
}
