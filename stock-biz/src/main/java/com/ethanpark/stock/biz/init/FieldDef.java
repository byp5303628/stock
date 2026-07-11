package com.ethanpark.stock.biz.init;

import lombok.Getter;

/**
 * 元数据字段定义。
 *
 * <p>描述 metadata_field 表中的一个字段，包含字段编码、业务含义、排序序号。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
@Getter
public class FieldDef {

    private final String fieldName;
    private final String businessMeaning;
    private final int sortOrder;

    public FieldDef(String fieldName, String businessMeaning, int sortOrder) {
        this.fieldName = fieldName;
        this.businessMeaning = businessMeaning;
        this.sortOrder = sortOrder;
    }

    public static FieldDef of(String fieldName, String businessMeaning, int sortOrder) {
        return new FieldDef(fieldName, businessMeaning, sortOrder);
    }
}
