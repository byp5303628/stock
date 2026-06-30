package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 切换模型版本请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class SwitchVersionRequest {
    @NotNull(message = "modelId 不能为空")
    private Long modelId;

    @NotNull(message = "version 不能为空")
    private Integer version;
}