package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 枚举使用统计 DTO。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class EnumUsageDTO {
    private Long enumId;
    private String enumName;
    private int totalRefCount;
    private int refModelCount;
    private int refFieldCount;
    private List<EnumRefDetailDTO> refDetails;
}
