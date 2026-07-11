package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.dto.FinancialDataDeleteRequest;
import com.ethanpark.stock.biz.dto.FinancialDataQueryRequest;
import com.ethanpark.stock.biz.dto.GenericBatchUpsertRequest;
import com.ethanpark.stock.biz.dto.GenericDataDTO;
import com.ethanpark.stock.biz.dto.GenericUpsertRequest;
import com.ethanpark.stock.biz.dto.ReplaceRangeRequest;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.service.GenericDataDomainService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一金融数据查询写入 Controller。
 *
 * <p>本模块是外部金融数据和内部计算结果的统一读写入口。
 * 所有数据通过 {@link GenericDataDomainService} 路由到对应的 fin_{{@code dataType}} 物理表，
 * 业务字段由 metadata 系统定义，无需为每类数据编写独立的 Controller。
 *
 * <p>所有接口使用 POST + JSON Body 通信，请求参数封装在专用的 Request DTO 中，
 * 通过 {@link Valid} + Bean Validation 注解统一校验。
 *
 * @author baiyunpeng04
 * @since 2025/07/02
 */
@RestController
@RequestMapping("/api/financial-data")
public class FinancialDataController {

    @Resource
    private GenericDataDomainService genericDataService;

    /**
     * 查询金融数据。
     *
     * <p>当请求中包含 modelCode + partitionDate 时走精确查询逻辑，
     * 返回单条记录；否则走分页查询逻辑，支持按 modelCode 和时间范围过滤。
     * 分页结果包含 total / page / size / pages 信息。
     *
     * @param request 查询请求体，包含 dataType / code 等查询参数
     * @return 单条记录（精确查询）或分页结果（分页查询）
     */
    @PostMapping("/query")
    public ResponseDTO<?> query(@RequestBody @Valid FinancialDataQueryRequest request) {
        String dataType = request.getDataType();
        String market = request.getMarket();
        String code = request.getCode();

        // 精确查询：dataType + market + code + modelCode + partitionDate
        if (request.getPartitionDate() != null && request.getModelCode() != null) {
            Result<GenericDataRecord> result = genericDataService.get(
                    dataType, market, code, request.getModelCode(), request.getPartitionDate());
            if (!result.isSuccess()) {
                return ResponseDTO.error(500, result.getMsg());
            }
            if (result.getData() == null) {
                return ResponseDTO.success();
            }
            return ResponseDTO.success(convert(result.getData()));
        }

        // 分页查询
        GenericDataQuery query = new GenericDataQuery();
        query.setMarket(market);
        query.setCode(code);
        query.setModelCode(request.getModelCode());
        query.setStartDate(request.getStartDate());
        query.setEndDate(request.getEndDate());
        query.setPage(request.getPage());
        query.setSize(request.getSize());

        PageResult<GenericDataRecord> pageResult = genericDataService.queryPage(dataType, query);

        PageResult<GenericDataDTO> dtoResult = new PageResult<>();
        dtoResult.setTotal(pageResult.getTotal());
        dtoResult.setPage(pageResult.getPage());
        dtoResult.setSize(pageResult.getSize());
        dtoResult.setPages(pageResult.getPages());
        dtoResult.setItems(pageResult.getItems().stream()
                .map(this::convert)
                .collect(Collectors.toList()));

        return ResponseDTO.success(dtoResult);
    }

    /**
     * 写入单条金融数据。
     *
     * <p>使用 upsert 语义：存在相同唯一键 (market + code + modelCode + partitionDate) 的记录则更新，
     * 不存在则插入。本接口主要供外部爬虫调用。
     *
     * @param request 单条写入请求体
     * @return 成功响应
     */
    @PostMapping("/upsert")
    public ResponseDTO<Void> upsert(@RequestBody @Valid GenericUpsertRequest request) {
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket(request.getMarket());
        record.setCode(request.getCode());
        record.setModelCode(request.getModelCode());
        record.setPartitionDate(request.getPartitionDate());
        record.setDataContent(request.getDataContent());
        record.setExtraInfo(request.getExtraInfo());
        genericDataService.upsert(request.getDataType(), record);
        return ResponseDTO.success();
    }

    /**
     * 批量写入金融数据。
     *
     * <p>使用 upsert 语义批量写入，适用于内部计算结果的批量回填。
     * 所有记录在单次 batchUpdate 中提交。
     *
     * @param request 批量写入请求体
     * @return 成功响应
     */
    @PostMapping("/batch-upsert")
    public ResponseDTO<Void> batchUpsert(@RequestBody @Valid GenericBatchUpsertRequest request) {
        List<GenericDataRecord> records = request.getRecords().stream().map(item -> {
            GenericDataRecord record = new GenericDataRecord();
            record.setMarket(item.getMarket());
            record.setCode(item.getCode());
            record.setModelCode(item.getModelCode());
            record.setPartitionDate(item.getPartitionDate());
            record.setDataContent(item.getDataContent());
            record.setExtraInfo(item.getExtraInfo());
            return record;
        }).collect(Collectors.toList());

        genericDataService.batchUpsert(request.getDataType(), records);
        return ResponseDTO.success();
    }

    /**
     * 替换指定时间范围内的数据（先删后插）。
     *
     * <p>将 (market + code + modelCode + startDate ~ endDate) 范围内的数据全部删除，
     * 然后用 records 中的新数据批量写入。整个操作在 {@code @Transactional} 中完成。
     * 适用于内部计算重跑时覆盖旧数据的场景。
     *
     * @param request 范围替换请求体
     * @return 成功响应
     */
    @PostMapping("/replace-range")
    public ResponseDTO<Void> replaceRange(@RequestBody @Valid ReplaceRangeRequest request) {
        if (request.getRecords().isEmpty()) {
            return ResponseDTO.success();
        }
        List<GenericDataRecord> records = request.getRecords().stream().map(item -> {
            GenericDataRecord record = new GenericDataRecord();
            record.setMarket(item.getMarket());
            record.setCode(item.getCode());
            record.setModelCode(item.getModelCode());
            record.setPartitionDate(item.getPartitionDate());
            record.setDataContent(item.getDataContent());
            record.setExtraInfo(item.getExtraInfo());
            return record;
        }).collect(Collectors.toList());

        ReplaceRangeRequest.RecordItemDTO first = request.getRecords().get(0);
        genericDataService.replaceRange(request.getDataType(), first.getMarket(),
                first.getCode(), first.getModelCode(),
                request.getStartDate(), request.getEndDate(), records);
        return ResponseDTO.success();
    }

    /**
     * 删除单条金融数据。
     *
     * <p>通过唯一键 (market + code + modelCode + partitionDate) 定位并删除。
     *
     * @param request 删除请求体
     * @return 成功响应
     */
    @PostMapping("/delete")
    public ResponseDTO<Void> delete(@RequestBody @Valid FinancialDataDeleteRequest request) {
        genericDataService.delete(request.getDataType(), request.getMarket(),
                request.getCode(), request.getModelCode(), request.getPartitionDate());
        return ResponseDTO.success();
    }

    // ——— helper ———

    private GenericDataDTO convert(GenericDataRecord record) {
        GenericDataDTO dto = new GenericDataDTO();
        dto.setId(record.getId());
        dto.setMarket(record.getMarket());
        dto.setCode(record.getCode());
        dto.setModelCode(record.getModelCode());
        dto.setPartitionDate(record.getPartitionDate());
        dto.setDataContent(record.getDataContent());
        dto.setExtraInfo(record.getExtraInfo());
        dto.setGmtCreate(record.getGmtCreate());
        dto.setGmtModified(record.getGmtModified());
        return dto;
    }

    // for testing
    void setGenericDataService(GenericDataDomainService genericDataService) {
        this.genericDataService = genericDataService;
    }
}
