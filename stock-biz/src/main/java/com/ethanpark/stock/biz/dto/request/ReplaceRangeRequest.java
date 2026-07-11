package com.ethanpark.stock.biz.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * 范围替换请求（先删后插）。
 *
 * <p>将指定 (market + code + modelCode + startDate ~ endDate) 范围内的数据全部删除，
 * 然后用 records 中的新数据批量写入。整个操作在单个事务中完成。
 *
 * @author baiyunpeng04
 * @since 2025/07/10
 */
@Getter
@Setter
public class ReplaceRangeRequest {

    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    @NotBlank(message = "startDate 不能为空")
    private String startDate;

    @NotBlank(message = "endDate 不能为空")
    private String endDate;

    @NotEmpty(message = "records 不能为空")
    @Valid
    private List<RecordItemDTO> records;

    @Getter
    @Setter
    public static class RecordItemDTO {
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
