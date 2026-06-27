package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 校验结果 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class ValidationResultDTO {
    private boolean valid;
    private List<ValidationErrorDTO> errors;
}
