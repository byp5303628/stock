package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class StrategyDetailDTO {
    private String name;

    private List<String> tags;

    private String description;

    private Integer verifyStockCnt = 0;

    private Integer totalStockCnt = 0;

    private List<HistogramItemDTO> histogramItemDTOS;

    /**
     * 算术平均
     */
    private String avgTradeIncr;

    public String getVerifyRate() {
        if (totalStockCnt == null
                || totalStockCnt == 0L) {
            return "尚未统计";
        }

        return String.format("%.2f%%", 1D * verifyStockCnt / totalStockCnt * 100);
    }
}
