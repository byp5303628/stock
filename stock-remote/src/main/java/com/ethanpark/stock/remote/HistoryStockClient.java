package com.ethanpark.stock.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ethanpark.stock.remote.model.StockBasic;
import com.ethanpark.stock.common.util.DateUtils;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Slf4j
@Service
public class HistoryStockClient {
    private static final String QFQ_URL_PREFIX = "https://web.ifzq.gtimg" +
            ".cn/appstock/app/fqkline/get?param=%s,day,%s,%s,500,qfq";

    private static final String HFQ_URL_PREFIX = "https://web.ifzq.gtimg" +
            ".cn/appstock/app/fqkline/get?param=%s,day,%s,%s,500,hfq";


    public List<StockBasic> queryStockHistory(String code, String startDate,
                                              String endDate, boolean front) {
        String key = getPrefix(code) + code;

        String startDateNew = DateUtils.plusDay(startDate);

        String url = String.format(front ? QFQ_URL_PREFIX : HFQ_URL_PREFIX, key, startDateNew, endDate);


        GetRequest getRequest = Unirest.get(url);

        HttpResponse<String> response = getRequest.asString();

//        log.info("查询结果: {}", response.getBody());

        JSONObject jsonObject = JSONObject.parseObject(response.getBody());

        JSONObject data = jsonObject.getJSONObject("data").getJSONObject(key);
        JSONArray jsonArray = data.getJSONArray(
                front ? "qfqday": "hfqday");
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }

        String name = data.getJSONObject("qt").getJSONArray(key).getString(1);

        List<List> array = jsonArray.toJavaList(List.class);

        List<StockBasic> dos = new ArrayList<>(300);

        for (List list : array) {
            List<String> strList = (List<String>) list;

            StockBasic stockDailyDigestDO = new StockBasic();
            stockDailyDigestDO.setCode(code);
            stockDailyDigestDO.setName(name);
            stockDailyDigestDO.setPartitionDate(strList.get(0));
            stockDailyDigestDO.setStartPrice(new BigDecimal(strList.get(1)));
            stockDailyDigestDO.setEndPrice(new BigDecimal(strList.get(2)));
            stockDailyDigestDO.setHighestPrice(new BigDecimal(strList.get(3)));
            stockDailyDigestDO.setLowestPrice(new BigDecimal(strList.get(4)));
            stockDailyDigestDO.setTotalValue(new BigDecimal(strList.get(5)));

            dos.add(stockDailyDigestDO);
        }

        return dos;
    }

    public String getPrefix(String code) {
        if (code.startsWith("600")) {
            return "sh";
        } else {
            return "sz";
        }
    }

    public static void main(String[] args) {
        HistoryStockClient client = new HistoryStockClient();

        List<StockBasic> stockBasics = client.queryStockHistory("600519", "2024-01-01", "2025-01" +
                "-01", true);

        System.out.println(JSONArray.toJSONString(stockBasics));
    }
}
