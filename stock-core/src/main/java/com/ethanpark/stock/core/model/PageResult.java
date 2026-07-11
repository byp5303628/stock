package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2025/07/02
 */
@Getter
@Setter
public class PageResult<T> {
    private List<T> items;

    private long total;

    private int page;

    private int size;

    private int pages;
}
