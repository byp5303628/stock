package com.ethanpark.stock.biz.trade;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/26
 */
@Service
public class TradePolicyFactory {

    @Resource
    private List<TradePolicy> policies;

    private Map<String, TradePolicy> policiesMap;

    @PostConstruct
    public void init() {
        policiesMap =
                policies.stream().collect(Collectors.toMap(i -> i.getName(), Function.identity()));
    }

    public TradePolicy getTradePolicy(String name) {
        return policiesMap.get(name);
    }

    public List<TradePolicy> getPolicies() {
        return policies;
    }
}
