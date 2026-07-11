package com.ethanpark.stock.biz.init;

import java.util.List;

/**
 * 元数据模型初始化器接口。
 *
 * <p>每个实现类对应一个 metadata 模型（如现金流量表、资产负债表），
 * 定义该模型的基础信息和所有字段。由 {@link InitializerRunner} 反射实例化并执行。
 *
 * <p>实现类必须提供无参构造器。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
public interface MetadataInitializer {

    /** 模型编码，对应 metadata_model.code */
    String getCode();

    /** 模型名称 */
    String getName();

    /** 模型描述 */
    String getDescription();

    /** 字段定义列表 */
    List<FieldDef> getFields();
}
