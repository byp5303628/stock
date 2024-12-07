package com.ethanpark.stock.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/26
 */
@Getter
@Setter
public class StrategyDTO {
    private String name;

    private List<String> tags;

    private String description;


}
