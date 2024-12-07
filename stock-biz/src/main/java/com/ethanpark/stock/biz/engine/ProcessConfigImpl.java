package com.ethanpark.stock.biz.engine;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: 柏云鹏
 * Date: 2022/4/20.
 */
public class ProcessConfigImpl implements ProcessConfig {
    private Map<String, ProcessStage> processStageMap;

    @Override
    public List<String> getActions(String stage) {
        ProcessStage processStage = processStageMap.get(stage);

        if (processStage == null) {
            return new ArrayList<String>();
        }

        return processStage.getActions();
    }

    @Override
    public String getRouter(String stage) {
        ProcessStage processStage = processStageMap.get(stage);

        if (processStage == null) {
            return "";
        }

        return processStage.getRouter();
    }

    public Map<String, ProcessStage> getProcessStageMap() {
        return processStageMap;
    }

    public void setProcessStageMap(Map<String, ProcessStage> processStageMap) {
        this.processStageMap = processStageMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
