package com.ethanpark.stock.biz.engine;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: 柏云鹏 Date: 2022/4/20.
 */
public class ProcessStage {
    private List<String> actions = new ArrayList<>();

    private String router = Router.DEFAULT_ROUTER;

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
