package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 枚举绑定请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class EnumBindRequest {
    @NotNull(message = "字段 ID 不能为空")
    private Long fieldId;

    @NotNull(message = "枚举 ID 不能为空")
    private Long enumId;
}
