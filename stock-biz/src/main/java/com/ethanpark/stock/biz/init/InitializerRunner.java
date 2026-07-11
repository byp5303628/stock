package com.ethanpark.stock.biz.init;

import com.ethanpark.stock.core.service.MetadataDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 初始化器统一执行器。
 *
 * <p>遍历 {@link InitializerDefinition} 中的所有枚举项，按类别依次执行初始化。
 * 新增初始化任务只需在枚举中添加新项，无需编写独立的 Initializer 类。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
@Slf4j
@Component
public class InitializerRunner {

    @Resource
    private MetadataDomainService metadataDomainService;

    @PostConstruct
    public void run() {
        log.info("开始执行初始化任务...");
        for (InitializerDefinition def : InitializerDefinition.values()) {
            try {
                def.initialize(metadataDomainService);
            } catch (Exception e) {
                log.error("初始化任务执行失败: code={}, name={}", def.getCode(), def.getName(), e);
            }
        }
        log.info("所有初始化任务执行完成");
    }
}
