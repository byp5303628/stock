package com.ethanpark.stock.biz.engine.config;

import com.alibaba.fastjson.JSON;
import com.ethanpark.stock.biz.engine.ProcessConfig;
import com.ethanpark.stock.biz.engine.ProcessConfigImpl;
import com.ethanpark.stock.biz.engine.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: 柏云鹏 Date: 2022/4/20.
 */
@Slf4j
@Service
public class ProcessConfigCacheImpl implements ProcessConfigCache {

    @Value("classpath:process/*.json")
    Resource[] resources;

    private Map<String, ProcessConfig> processConfigMap;

    @PostConstruct
    private void init() {
        if (resources == null || resources.length == 0) {
            log.warn("ProcessConfigCache init failed! Templates is empty!");
            return;
        }

        Map<String, ProcessConfig> tempMap = new HashMap<>();

        try {
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                String key = fileName.substring(0, fileName.indexOf(".json"));

                String content = IOUtils.toString(resource.getInputStream(),
                        StandardCharsets.UTF_8);

                ProcessConfig config = JSON.parseObject(content, ProcessConfigImpl.class);

                tempMap.put(key, config);
            }

            processConfigMap = tempMap;

            log.info("ProcessConfigCache init success! configMap="
                    + ToStringBuilder.reflectionToString(processConfigMap));
        } catch (Exception e) {
            log.error("ProcessConfigCache init failed!", e);
        }
    }

    @Override
    public ProcessConfig getProcessConfig(ProcessContext processContext) {
        String key = processContext.getProductCode() + "_" + processContext.getBusinessCode();
        ProcessConfig config = processConfigMap.get(key);
        return config;
    }
}
