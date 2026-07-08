package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.metadata.MetadataModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteDispatcherTest {

    @Mock
    private MetadataDomainService metadataDomainService;

    private RouteDispatcher routeDispatcher;

    @BeforeEach
    void setUp() {
        routeDispatcher = new RouteDispatcher();
        routeDispatcher.setMetadataDomainService(metadataDomainService);
    }

    @Test
    @DisplayName("resolveTable 返回 fin_{dataType} 格式的表名")
    void resolveTable_existingDataType_returnsPrefixedTableName() {
        MetadataModel model = new MetadataModel();
        model.setCode("report");
        when(metadataDomainService.getModelByCode("report")).thenReturn(model);

        String table = routeDispatcher.resolveTable("report");

        assertThat(table).isEqualTo("fin_report");
        verify(metadataDomainService).getModelByCode("report");
    }

    @Test
    @DisplayName("resolveTable 未注册的 dataType 抛异常")
    void resolveTable_unknownDataType_throws() {
        when(metadataDomainService.getModelByCode("unknown")).thenReturn(null);

        assertThatThrownBy(() -> routeDispatcher.resolveTable("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未知 dataType");
    }

    @Test
    @DisplayName("resolveTable 走本地缓存，不重复查 DB")
    void resolveTable_cached_doesNotCallDbAgain() {
        MetadataModel model = new MetadataModel();
        model.setCode("report");
        when(metadataDomainService.getModelByCode("report")).thenReturn(model);

        // 第一次调用：查 DB
        routeDispatcher.resolveTable("report");
        // 第二次调用：走缓存
        routeDispatcher.resolveTable("report");

        verify(metadataDomainService, times(1)).getModelByCode("report");
    }

    @Test
    @DisplayName("resolveModel 校验成功后返回 MetadataModel")
    void resolveModel_validModelCode_returnsModel() {
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("dataType", "report");
        MetadataModel model = new MetadataModel();
        model.setCode("cash_flow_statement");
        model.setExtInfo(extInfo);
        when(metadataDomainService.getModelByCode("cash_flow_statement")).thenReturn(model);

        MetadataModel result = routeDispatcher.resolveModel("report", "cash_flow_statement");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("cash_flow_statement");
    }

    @Test
    @DisplayName("resolveModel 当 modelCode 不属于 dataType 时抛异常")
    void resolveModel_wrongDataType_throws() {
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("dataType", "kline");
        MetadataModel model = new MetadataModel();
        model.setCode("cash_flow_statement");
        model.setExtInfo(extInfo);
        when(metadataDomainService.getModelByCode("cash_flow_statement")).thenReturn(model);

        assertThatThrownBy(() -> routeDispatcher.resolveModel("report", "cash_flow_statement"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("modelCode cash_flow_statement 不属于 dataType report");
    }
}
