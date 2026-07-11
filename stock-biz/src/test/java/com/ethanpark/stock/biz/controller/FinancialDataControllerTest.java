package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.handler.GlobalExceptionHandler;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.service.GenericDataDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialDataControllerTest {

    @Mock
    private GenericDataDomainService genericDataService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FinancialDataController controller = new FinancialDataController();
        controller.setGenericDataService(genericDataService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("场景：query 精确查询 — modelCode + partitionDate 定位单条")
    class QueryExact {

        @Test
        @DisplayName("存在记录时返回单条数据")
        void found_returnsRecord() throws Exception {
            GenericDataRecord record = new GenericDataRecord();
            record.setMarket("SH");
            record.setCode("000001");
            record.setPartitionDate("2024-12-31");
            when(genericDataService.get(eq("report"), eq("SH"), eq("000001"),
                    eq("cash_flow_statement"), eq("2024-12-31"))).thenReturn(Result.ok(record));

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.code").value("000001"));
        }

        @Test
        @DisplayName("不存在时返回 data null，code 仍为 200")
        void notFound_returnsNullData() throws Exception {
            when(genericDataService.get(anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(Result.ok(null));

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"999999\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("modelCode + partitionDate 传输给 GenericDataDomainService.get()")
        void delegatesToGetMethod() throws Exception {
            when(genericDataService.get(anyString(), anyString(), anyString(),
                    anyString(), anyString())).thenReturn(Result.ok(null));

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"}"));

            verify(genericDataService).get("report", "SH", "000001",
                    "cash_flow_statement", "2024-12-31");
        }
    }

    @Nested
    @DisplayName("场景：query 分页查询 — 无 partitionDate 或 modelCode")
    class QueryPage {

        @Captor
        private ArgumentCaptor<com.ethanpark.stock.core.model.GenericDataQuery> queryCaptor;

        @Test
        @DisplayName("不传 partitionDate 时走分页查询")
        void withoutPartitionDate_usesPageQuery() throws Exception {
            PageResult<GenericDataRecord> pageResult = new PageResult<>();
            pageResult.setItems(Collections.emptyList());
            pageResult.setTotal(0);
            pageResult.setPage(1);
            pageResult.setSize(20);
            when(genericDataService.queryPage(eq("report"), any())).thenReturn(pageResult);

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("不传 modelCode 时也走分页查询")
        void withoutModelCode_usesPageQuery() throws Exception {
            PageResult<GenericDataRecord> pageResult = new PageResult<>();
            pageResult.setItems(Collections.emptyList());
            pageResult.setTotal(0);
            pageResult.setPage(1);
            pageResult.setSize(20);
            when(genericDataService.queryPage(eq("report"), any())).thenReturn(pageResult);

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"code\":\"000001\"," +
                            "\"partitionDate\":\"2024-12-31\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("page/size 默认值为 1/20")
        void defaultPageAndSize() throws Exception {
            PageResult<GenericDataRecord> pageResult = new PageResult<>();
            pageResult.setItems(Collections.emptyList());
            pageResult.setTotal(0);
            pageResult.setPage(1);
            pageResult.setSize(20);
            when(genericDataService.queryPage(eq("report"), queryCaptor.capture())).thenReturn(pageResult);

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"code\":\"000001\"}"));

            assertThat(queryCaptor.getValue().getPage()).isEqualTo(1);
            assertThat(queryCaptor.getValue().getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("market 默认值为 SH")
        void defaultMarket() throws Exception {
            PageResult<GenericDataRecord> pageResult = new PageResult<>();
            pageResult.setItems(Collections.emptyList());
            pageResult.setTotal(0);
            pageResult.setPage(1);
            pageResult.setSize(20);
            when(genericDataService.queryPage(eq("report"), queryCaptor.capture())).thenReturn(pageResult);

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"code\":\"000001\"}"));

            assertThat(queryCaptor.getValue().getMarket()).isEqualTo("SH");
        }

        @Test
        @DisplayName("startDate/endDate 传递给分页查询")
        void dateRangePassedToService() throws Exception {
            PageResult<GenericDataRecord> pageResult = new PageResult<>();
            pageResult.setItems(Collections.emptyList());
            pageResult.setTotal(0);
            pageResult.setPage(1);
            pageResult.setSize(20);
            when(genericDataService.queryPage(eq("report"), queryCaptor.capture())).thenReturn(pageResult);

            mockMvc.perform(post("/api/financial-data/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"code\":\"000001\"," +
                            "\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\"}"));

            assertThat(queryCaptor.getValue().getStartDate()).isEqualTo("2024-01-01");
            assertThat(queryCaptor.getValue().getEndDate()).isEqualTo("2024-12-31");
        }
    }

    @Nested
    @DisplayName("场景：upsert 写入")
    class Upsert {

        @Test
        @DisplayName("写入成功返回 code 200")
        void upsert_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/financial-data/upsert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"," +
                            "\"dataContent\":{\"totalRevenue\":1000}}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("dataType 等必填字段缺失时返回 202")
        void missingFields_returnsValidationError() throws Exception {
            mockMvc.perform(post("/api/financial-data/upsert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(jsonPath("$.code").value(202));
        }
    }

    @Nested
    @DisplayName("场景：batch-upsert 批量写入")
    class BatchUpsert {

        @Test
        @DisplayName("批量写入成功返回 code 200")
        void batchUpsert_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/financial-data/batch-upsert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\"," +
                            "\"records\":[{\"market\":\"SH\",\"code\":\"000001\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"," +
                            "\"dataContent\":{\"totalRevenue\":1000}}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("records 为空数组时返回 202")
        void emptyRecords_returnsValidationError() throws Exception {
            mockMvc.perform(post("/api/financial-data/batch-upsert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"records\":[]}"))
                    .andExpect(jsonPath("$.code").value(202));
        }
    }

    @Nested
    @DisplayName("场景：replace-range 范围替换")
    class ReplaceRange {

        @Test
        @DisplayName("替换成功返回 code 200")
        void replaceRange_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/financial-data/replace-range")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\"," +
                            "\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\"," +
                            "\"records\":[{\"market\":\"SH\",\"code\":\"000001\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"," +
                            "\"dataContent\":{\"totalRevenue\":1000}}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("records 空数组时返回 202")
        void emptyRecords_returnsValidationError() throws Exception {
            mockMvc.perform(post("/api/financial-data/replace-range")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\"," +
                            "\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\"," +
                            "\"records\":[]}"))
                    .andExpect(jsonPath("$.code").value(202));
        }
    }

    @Nested
    @DisplayName("场景：delete 删除")
    class Delete {

        @Test
        @DisplayName("删除成功返回 code 200")
        void delete_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/financial-data/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"," +
                            "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("必填字段缺失时返回 202")
        void missingFields_returnsValidationError() throws Exception {
            mockMvc.perform(post("/api/financial-data/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(jsonPath("$.code").value(202));
        }
    }
}
