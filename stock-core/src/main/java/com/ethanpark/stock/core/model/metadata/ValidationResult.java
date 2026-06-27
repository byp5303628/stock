package com.ethanpark.stock.core.model.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Schema 校验结果。
 *
 * @author baiyunpeng04
 */
@Getter
@Setter
public class ValidationResult {
    private boolean valid = true;
    private List<ValidationError> errors = new ArrayList<>();

    public void addError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
        this.valid = false;
    }

    /**
     * Schema 校验错误详情。
     *
     * @author baiyunpeng04
     */
    @Getter
    @Setter
    public static class ValidationError {
        private String field;
        private String message;

        public ValidationError() {}

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
