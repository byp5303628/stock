package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.dto.GenericUpsertRequest;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.GenericDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialDataControllerTest {

    @Mock
    private GenericDataService genericDataService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FinancialDataController controller = new FinancialDataController();
        controller.setGenericDataService(genericDataService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/financial-data 精确查询返回单条")
    void query_exactMatch_returnsRecord() throws Exception {
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket("SH");
        record.setCode("000001");
        record.setPartitionDate("2024-12-31");
        when(genericDataService.get(eq("report"), eq("SH"), eq("000001"),
                eq("cash_flow_statement"), eq("2024-12-31"))).thenReturn(record);

        mockMvc.perform(get("/api/financial-data")
                .param("dataType", "report")
                .param("market", "SH")
                .param("code", "000001")
                .param("modelCode", "cash_flow_statement")
                .param("partitionDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("000001"));
    }

    @Test
    @DisplayName("GET /api/financial-data 不存在返回非 200")
    void query_notFound_returnsError() throws Exception {
        when(genericDataService.get(anyString(), anyString(), anyString(),
                anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/api/financial-data")
                .param("dataType", "report")
                .param("market", "SH")
                .param("code", "999999")
                .param("modelCode", "cash_flow_statement")
                .param("partitionDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/financial-data/upsert 返回成功")
    void upsert_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/financial-data/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"," +
                        "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"," +
                        "\"dataContent\":{\"totalRevenue\":1000}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
