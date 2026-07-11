package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.model.Result;

import java.util.List;

public interface GenericDataDomainService {

    /**
     * 精确查询单条记录。
     *
     * <p>返回 {@link Result} 包裹的结果，上游通过 {@code result.isSuccess()} 检查操作是否成功，
     * 通过 {@code result.getData()} 获取数据（为 null 表示不存在）。
     */
    Result<GenericDataRecord> get(String dataType, String market, String code,
                                  String modelCode, String partitionDate);

    /**
     * 分页查询。
     *
     * <p>返回 {@link PageResult}，本身即包含分页信息，不额外包裹 Result。
     */
    PageResult<GenericDataRecord> queryPage(String dataType, GenericDataQuery query);

    void upsert(String dataType, GenericDataRecord record);

    void batchUpsert(String dataType, List<GenericDataRecord> records);

    void replaceRange(String dataType, String market, String code, String modelCode,
                      String startDate, String endDate, List<GenericDataRecord> records);

    void delete(String dataType, String market, String code, String modelCode, String partitionDate);

    void deleteByRange(String dataType, String market, String code, String modelCode,
                       String startDate, String endDate);
}
