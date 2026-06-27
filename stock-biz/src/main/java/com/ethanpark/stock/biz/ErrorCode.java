package com.ethanpark.stock.biz;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
public enum ErrorCode {
    SUCCESS(200, "成功"),
    ILLEGAL_PARAM(202, "参数异常"),
    SYSTEM_ERROR(208, "系统异常"),

    /** 元数据相关 */
    METADATA_MODEL_NOT_FOUND(42001, "元数据模型不存在"),
    METADATA_ENUM_NOT_FOUND(42002, "枚举不存在"),
    METADATA_FIELD_NOT_FOUND(42003, "字段不存在"),
    METADATA_ENUM_HAS_REFS(42004, "枚举被引用，无法删除"),
    METADATA_CODE_DUPLICATE(42005, "编码重复"),
    ;
    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
