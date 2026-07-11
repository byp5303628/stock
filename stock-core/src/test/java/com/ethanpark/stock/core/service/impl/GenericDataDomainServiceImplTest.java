package com.ethanpark.stock.core.service.impl;

import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.service.GenericDataDomainService;
import com.ethanpark.stock.core.route.RouteDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericDataDomainServiceImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private RouteDispatcher routeDispatcher;

    private GenericDataDomainService genericDataService;

    @BeforeEach
    void setUp() {
        GenericDataDomainServiceImpl impl = new GenericDataDomainServiceImpl();
        impl.setJdbcTemplate(jdbcTemplate);
        impl.setRouteDispatcher(routeDispatcher);
        genericDataService = impl;
    }

    @Test
    @DisplayName("get() 返回单条记录，success=true，data 非空")
    void get_existingRecord_returnsRecord() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("market", "SH");
        row.put("code", "000001");
        row.put("model_code", "cash_flow_statement");
        row.put("partition_date", "2024-12-31");
        row.put("data_content", "{\"totalRevenue\":1000}");
        row.put("extra_info", "{}");
        row.put("gmt_create", new Date());
        row.put("gmt_modified", new Date());
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.singletonList(row));

        Result<GenericDataRecord> result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getCode()).isEqualTo("000001");
        assertThat(result.getData().getMarket()).isEqualTo("SH");
        assertThat(result.getData().getModelCode()).isEqualTo("cash_flow_statement");
        assertThat(result.getData().getPartitionDate()).isEqualTo("2024-12-31");
        assertThat(result.getData().getDataContent()).containsEntry("totalRevenue", 1000);
    }

    @Test
    @DisplayName("get() 不存在时返回 success=true，data 为 null")
    void get_notFound_returnsNull() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.emptyList());

        Result<GenericDataRecord> result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("get() DB 异常时返回 success=false")
    void get_dbError_returnsFail() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        Result<GenericDataRecord> result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("查询失败");
    }

    @Test
    @DisplayName("upsert() 使用 ON DUPLICATE KEY UPDATE 语义")
    void upsert_usesUpsertSemantics() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket("SH");
        record.setCode("000001");
        record.setModelCode("cash_flow_statement");
        record.setPartitionDate("2024-12-31");
        Map<String, Object> content = new HashMap<>();
        content.put("totalRevenue", 1000);
        record.setDataContent(content);

        genericDataService.upsert("report", record);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramCaptor.capture());

        assertThat(sqlCaptor.getValue()).contains("duplicate key update");
        assertThat(sqlCaptor.getValue()).contains("fin_report");
    }

    @Test
    @DisplayName("batchUpsert() 使用 batchUpdate 批量提交")
    void batchUpsert_usesBatchUpdate() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        GenericDataRecord record1 = new GenericDataRecord();
        record1.setMarket("SH");
        record1.setCode("000001");
        record1.setModelCode("cash_flow_statement");
        record1.setPartitionDate("2024-12-31");
        GenericDataRecord record2 = new GenericDataRecord();
        record2.setMarket("SZ");
        record2.setCode("000002");
        record2.setModelCode("cash_flow_statement");
        record2.setPartitionDate("2024-12-31");

        genericDataService.batchUpsert("report", List.of(record1, record2));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource[]> batchCaptor = ArgumentCaptor.forClass(MapSqlParameterSource[].class);
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), batchCaptor.capture());

        assertThat(sqlCaptor.getValue()).contains("duplicate key update");
        assertThat(batchCaptor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("batchUpsert() 空列表不执行任何操作")
    void batchUpsert_emptyList_doesNothing() {
        genericDataService.batchUpsert("report", Collections.emptyList());
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    @DisplayName("delete() 生成正确的 SQL")
    void delete_generatesCorrectSql() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");

        genericDataService.delete("report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), any(MapSqlParameterSource.class));
        assertThat(sqlCaptor.getValue()).contains("delete from fin_report");
        assertThat(sqlCaptor.getValue()).contains("market = :market");
        assertThat(sqlCaptor.getValue()).contains("partition_date = :partitionDate");
    }

    @Test
    @DisplayName("deleteByRange() 生成范围删除 SQL")
    void deleteByRange_generatesRangeSql() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");

        genericDataService.deleteByRange("report", "SH", "000001", "cash_flow_statement",
                "2024-01-01", "2024-12-31");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), any(MapSqlParameterSource.class));
        assertThat(sqlCaptor.getValue()).contains("partition_date between");
    }

    @Test
    @DisplayName("replaceRange() 先删除后批量写入")
    void replaceRange_deletesThenInserts() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket("SH");
        record.setCode("000001");
        record.setModelCode("cash_flow_statement");
        record.setPartitionDate("2024-12-31");

        genericDataService.replaceRange("report", "SH", "000001", "cash_flow_statement",
                "2024-01-01", "2024-12-31", List.of(record));

        // deleteByRange 调用 update，batchUpsert 调用 batchUpdate
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    @DisplayName("queryPage() 返回分页结果")
    void queryPage_returnsPageResult() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        GenericDataQuery query = new GenericDataQuery();
        query.setMarket("SH");
        query.setCode("000001");
        query.setPage(1);
        query.setSize(20);

        when(jdbcTemplate.queryForObject(contains("count"), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(contains("limit"), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.singletonList(Map.of("id", 1L)));

        PageResult<GenericDataRecord> result = genericDataService.queryPage("report", query);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(1);
    }
}
