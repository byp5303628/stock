package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * dataType 到物理表名的路由分发器。
 *
 * <p>通过本地缓存查询 metadata_model 表，确认 dataType 已注册后返回物理表名。
 * 物理表名格式固定为 fin_{dataType}。
 */
@Component
public class RouteDispatcher {

    @Resource
    private MetadataDomainService metadataDomainService;

    private final Cache<String, MetadataModel> routeCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    /**
     * 解析 dataType 为物理表名。
     *
     * @param dataType 数据类型编码
     * @return 物理表名，如 "fin_report"
     * @throws IllegalArgumentException dataType 未注册时抛出
     */
    public String resolveTable(String dataType) {
        MetadataModel model = routeCache.get(dataType, key -> {
            MetadataModel m = metadataDomainService.getModelByCode(key);
            if (m == null) {
                throw new IllegalArgumentException("未知 dataType: " + key);
            }
            return m;
        });
        return "fin_" + dataType;
    }

    /**
     * 校验并解析 modelCode，确认其属于指定的 dataType。
     *
     * @param dataType  数据类型
     * @param modelCode 模型编码
     * @return MetadataModel 对象
     * @throws IllegalArgumentException 校验不通过时抛出
     */
    public MetadataModel resolveModel(String dataType, String modelCode) {
        MetadataModel model = metadataDomainService.getModelByCode(modelCode);
        if (model == null) {
            throw new IllegalArgumentException("未知 modelCode: " + modelCode);
        }
        Map<String, Object> extInfo = model.getExtInfo();
        if (extInfo == null || !dataType.equals(extInfo.get("dataType"))) {
            throw new IllegalArgumentException(
                    "modelCode " + modelCode + " 不属于 dataType " + dataType);
        }
        return model;
    }

    /**
     * 校验 dataType 是否存在（不走缓存穿透，仅查缓存）。
     */
    public boolean exists(String dataType) {
        return routeCache.getIfPresent(dataType) != null;
    }

    // for testing — setter injection
    public void setMetadataDomainService(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }
}
