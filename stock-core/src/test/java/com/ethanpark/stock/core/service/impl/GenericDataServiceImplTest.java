package com.ethanpark.stock.core.service.impl;

import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.GenericDataService;
import com.ethanpark.stock.core.service.RouteDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericDataServiceImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private RouteDispatcher routeDispatcher;

    private GenericDataService genericDataService;

    @BeforeEach
    void setUp() {
        GenericDataServiceImpl impl = new GenericDataServiceImpl();
        impl.setJdbcTemplate(jdbcTemplate);
        impl.setRouteDispatcher(routeDispatcher);
        genericDataService = impl;
    }

    @Test
    @DisplayName("get() 返回单条记录")
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

        GenericDataRecord result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("000001");
        assertThat(result.getMarket()).isEqualTo("SH");
        assertThat(result.getModelCode()).isEqualTo("cash_flow_statement");
        assertThat(result.getPartitionDate()).isEqualTo("2024-12-31");
        assertThat(result.getDataContent()).containsEntry("totalRevenue", 1000);
    }

    @Test
    @DisplayName("get() 不存在时返回 null")
    void get_notFound_returnsNull() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.emptyList());

        GenericDataRecord result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result).isNull();
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
