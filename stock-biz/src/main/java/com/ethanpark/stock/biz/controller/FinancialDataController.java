package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.dto.GenericBatchUpsertRequest;
import com.ethanpark.stock.biz.dto.GenericDataDTO;
import com.ethanpark.stock.biz.dto.GenericUpsertRequest;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.GenericDataDomainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/financial-data")
public class FinancialDataController {

    @Resource
    private GenericDataDomainService genericDataService;

    @GetMapping
    public ResponseDTO<?> query(
            @RequestParam String dataType,
            @RequestParam(required = false, defaultValue = "SH") String market,
            @RequestParam String code,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String partitionDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        // 精确查询：dataType + market + code + modelCode + partitionDate
        if (partitionDate != null && modelCode != null) {
            GenericDataRecord record = genericDataService.get(
                    dataType, market, code, modelCode, partitionDate);
            if (record == null) {
                return ResponseDTO.success();
            }
            return ResponseDTO.success(convert(record));
        }

        // 分页查询
        GenericDataQuery query = new GenericDataQuery();
        query.setMarket(market);
        query.setCode(code);
        query.setModelCode(modelCode);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setPage(page);
        query.setSize(size);

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

    @PostMapping("/replace-range")
    public ResponseDTO<Void> replaceRange(@RequestBody @Valid GenericBatchUpsertRequest request) {
        if (request.getRecords().isEmpty()) return ResponseDTO.success();
        GenericBatchUpsertRequest.RecordItem first = request.getRecords().get(0);
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

        genericDataService.replaceRange(request.getDataType(), first.getMarket(),
                first.getCode(), first.getModelCode(), first.getPartitionDate(),
                first.getPartitionDate(), records);
        return ResponseDTO.success();
    }

    @PostMapping("/delete")
    public ResponseDTO<Void> delete(
            @RequestParam String dataType,
            @RequestParam String market,
            @RequestParam String code,
            @RequestParam String modelCode,
            @RequestParam String partitionDate) {
        genericDataService.delete(dataType, market, code, modelCode, partitionDate);
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
