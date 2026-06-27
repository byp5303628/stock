package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 校验错误 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class ValidationErrorDTO {
    private String field;
    private String message;
}
