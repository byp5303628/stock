package com.ethanpark.stock.biz.cal.impl;

import com.ethanpark.stock.core.model.MacdStat;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/16
 */
public class MacdStatStatisticsStrategyTest {

    @Test
    public void test() {
        List<BigDecimal> list = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            list.add(new BigDecimal(i));
        }

        List<MacdStat> ema = new MacdStatStatisticsStrategy(10, 20).getEma(list, 12, 26);

        Assert.assertNotNull(ema);

        for (MacdStat macdStat : ema) {
            System.out.println(macdStat.getEmaStart() + "  " + macdStat.getEmaEnd());
        }
    }


}