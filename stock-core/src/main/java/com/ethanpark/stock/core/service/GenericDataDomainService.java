package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;

import java.util.List;

public interface GenericDataDomainService {

    GenericDataRecord get(String dataType, String market, String code,
                          String modelCode, String partitionDate);

    PageResult<GenericDataRecord> queryPage(String dataType, GenericDataQuery query);

    List<GenericDataRecord> queryRange(String dataType, String market, String code,
                                       String modelCode, String startDate, String endDate);

    List<GenericDataRecord> queryLatest(String dataType, String market, String code, int limit);

    List<GenericDataRecord> queryLatestByModel(String dataType, String market, String code);

    void upsert(String dataType, GenericDataRecord record);

    void batchUpsert(String dataType, List<GenericDataRecord> records);

    void replaceRange(String dataType, String market, String code, String modelCode,
                      String startDate, String endDate, List<GenericDataRecord> records);

    void delete(String dataType, String market, String code, String modelCode, String partitionDate);

    void deleteByRange(String dataType, String market, String code, String modelCode,
                       String startDate, String endDate);
}
