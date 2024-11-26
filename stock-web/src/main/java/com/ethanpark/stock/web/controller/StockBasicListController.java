package com.ethanpark.stock.web.controller;

import com.ethanpark.stock.core.service.StockBasicDomainService;
import com.ethanpark.stock.remote.model.StockBasic;
import com.ethanpark.stock.web.dto.ResponseDTO;
import com.ethanpark.stock.web.dto.StockBasicDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/25
 */
@RestController("/api/stock-basic")
public class StockBasicListController {

    @Resource
    private StockBasicDomainService stockBasicDomainService;

    @GetMapping("/stock-detail.json")
    public ResponseDTO<StockBasicDTO> getStockBasicList(@RequestParam("code") String code) {

        StockBasic stockBasic = stockBasicDomainService.getLatestStockBasic(code);



    }
}
