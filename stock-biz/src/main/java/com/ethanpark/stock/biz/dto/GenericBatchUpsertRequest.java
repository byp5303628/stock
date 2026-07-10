package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GenericBatchUpsertRequest {
    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    @NotEmpty(message = "records 不能为空")
    @Valid
    private List<RecordItem> records;

    @Getter
    @Setter
    public static class RecordItem {
        @NotBlank(message = "市场不能为空")
        private String market;
        @NotBlank(message = "股票代码不能为空")
        private String code;
        @NotBlank(message = "模型编码不能为空")
        private String modelCode;
        @NotBlank(message = "分区日期不能为空")
        private String partitionDate;
        private Map<String, Object> dataContent;
        private Map<String, Object> extraInfo;
    }
}
