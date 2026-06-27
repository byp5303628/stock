package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 枚举使用统计信息。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class EnumUsage {
    private Long enumId;
    private String enumName;
    private int totalRefCount;
    private int refModelCount;
    private int refFieldCount;
    private List<RefDetail> refDetails;

    /**
     * 模型引用详情。
     *
     * @author baiyunpeng04
     */
    @Getter
    @Setter
    public static class RefDetail {
        private Long modelId;
        private String modelName;
        private String modelType;
        private List<RefField> fields;
    }

    /**
     * 引用字段信息。
     *
     * @author baiyunpeng04
     */
    @Getter
    @Setter
    public static class RefField {
        private Long fieldId;
        private String fieldName;

        public RefField() {}

        public RefField(Long fieldId, String fieldName) {
            this.fieldId = fieldId;
            this.fieldName = fieldName;
        }
    }
}
