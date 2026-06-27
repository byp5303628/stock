package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发布模型请求。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class PublishModelRequest {
    @NotNull(message = "modelId 不能为空")
    private Long modelId;

    @Size(max = 512, message = "版本说明不超过 512 字")
    private String versionDesc;
}