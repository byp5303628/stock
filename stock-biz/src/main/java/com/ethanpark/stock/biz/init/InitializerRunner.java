package com.ethanpark.stock.biz.init;

import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.MetadataDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

/**
 * 初始化器统一执行器。
 *
 * <p>遍历 {@link InitializerDefinition} 所有枚举项，按 {@link InitializerDefinition.Category}
 * 路由到对应的执行逻辑：
 * <ul>
 *   <li>{@link InitializerDefinition.Category#METADATA} — 反射实例化
 *       {@link MetadataInitializer}，创建 metadata model + field</li>
 *   <li>{@link InitializerDefinition.Category#CRAWLER} — 预留，由爬虫 handler 接管</li>
 * </ul>
 *
 * <p>新增初始化任务只需在 {@link InitializerDefinition} 中添加枚举项 + 实现接口，
 * 无需编写新的 {@code @PostConstruct} 初始化类。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
@Slf4j
@Component
public class InitializerRunner {

    private static final String MODEL_TYPE = "financial_statement";

    @Resource
    private MetadataDomainService metadataDomainService;

    @PostConstruct
    public void run() {
        log.info("开始执行初始化任务...");
        for (InitializerDefinition def : InitializerDefinition.values()) {
            try {
                execute(def);
            } catch (Exception e) {
                log.error("初始化任务执行失败: code={}, handler={}",
                        def.name(), def.getHandlerClass().getSimpleName(), e);
            }
        }
        log.info("所有初始化任务执行完成");
    }

    @SuppressWarnings("unchecked")
    private void execute(InitializerDefinition def) throws Exception {
        switch (def.getCategory()) {
            case METADATA:
                Class<? extends MetadataInitializer> metadataClass =
                        (Class<? extends MetadataInitializer>) def.getHandlerClass();
                MetadataInitializer initializer = metadataClass.getDeclaredConstructor().newInstance();
                initMetadata(initializer);
                break;
            case CRAWLER:
                log.info("爬虫任务已注册，待 handler 接管: handler={}",
                        def.getHandlerClass().getSimpleName());
                break;
        }
    }

    private void initMetadata(MetadataInitializer initializer) {
        String code = initializer.getCode();
        if (metadataDomainService.getModelByCode(code) != null) {
            log.info("模型已存在，跳过: {}", code);
            return;
        }

        // 创建模型
        MetadataModel model = new MetadataModel();
        model.setCode(code);
        model.setName(initializer.getName());
        model.setModelType(MODEL_TYPE);
        model.setDescription(initializer.getDescription());
        model.setExtInfo(Map.of("dataType", "report"));

        var modelResult = metadataDomainService.saveModel(model);
        if (!modelResult.isSuccess()) {
            log.error("创建元数据模型失败: code={}, msg={}", code, modelResult.getMsg());
            throw new IllegalStateException("创建元数据模型失败: " + code + ", " + modelResult.getMsg());
        }
        Long modelId = modelResult.getData().getId();

        // 创建字段
        for (FieldDef fieldDef : initializer.getFields()) {
            MetadataField field = new MetadataField();
            field.setModelId(modelId);
            field.setFieldName(fieldDef.getFieldName());
            field.setFieldType("DECIMAL");
            field.setBusinessMeaning(fieldDef.getBusinessMeaning());
            field.setSortOrder(fieldDef.getSortOrder());

            var fieldResult = metadataDomainService.saveField(field);
            if (!fieldResult.isSuccess()) {
                log.error("创建元数据字段失败: modelId={}, fieldName={}, msg={}",
                        modelId, fieldDef.getFieldName(), fieldResult.getMsg());
                throw new IllegalStateException(
                        "创建元数据字段失败: " + fieldDef.getFieldName() + ", " + fieldResult.getMsg());
            }
        }

        log.info("元数据模型初始化完成: code={}, name={}, 字段数={}",
                code, initializer.getName(), initializer.getFields().size());
    }

    // for testing
    void setMetadataDomainService(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }
}
