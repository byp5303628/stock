package com.ethanpark.stock.web.controller;

import com.ethanpark.stock.core.service.StockBasicDomainService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/25
 */
@RestController("/api/stock-basic")
public class StockBasicListController {

    @Resource
    private StockBasicDomainService stockBasicDomainService;


}
