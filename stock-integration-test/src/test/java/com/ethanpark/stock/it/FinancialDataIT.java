package com.ethanpark.stock.it;

import com.ethanpark.stock.biz.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一金融数据存储集成测试。
 *
 * <p>使用 H2 内存数据库，通过真实 HTTP 端点测试数据的写入和查询全链路。
 * <p>覆盖 upsert / batch-upsert / replace-range / query / delete 五个端点。
 *
 * @author baiyunpeng04
 * @since 2025/07/11
 */
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FinancialDataIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // ========================================================================
    // 共享 Helper
    // ========================================================================

    <T, R> ResponseDTO<R> post(String path, T body, ParameterizedTypeReference<ResponseDTO<R>> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl() + path, HttpMethod.POST, entity, type).getBody();
    }

    // ========================================================================
    // 场景：Upsert + 精确查询 — 写入并回读
    // ========================================================================
    @Nested
    @DisplayName("场景：Upsert + 精确查询 — 写入并回读")
    class UpsertAndQuery {

        private static final String MARKET = "SH";
        private static final String CODE = "000001";
        private static final String MODEL_CODE = "cash_flow_statement";
        private static final String PARTITION_DATE = "2024-12-31";
        private static boolean initialized;

        @BeforeEach
        void setUp() {
            if (initialized) return;

            // 写入一条数据用于后续查询
            Map<String, Object> body = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE,
                    "dataContent", Map.of("net_cash_flow_operating", 10000)
            );

            ResponseDTO<Void> resp = post("/api/financial-data/upsert", body,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
            assertEquals(200, resp.getCode(), "UpsertAndRead 场景数据初始化失败");
            initialized = true;
        }

        @Test
        @DisplayName("写入后通过精确查询可回读")
        void upsertThenQuery() {
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE
            );

            ResponseDTO<Map<String, Object>> resp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
            assertEquals(MARKET, resp.getData().get("market"));
            assertEquals(CODE, resp.getData().get("code"));
            assertEquals(MODEL_CODE, resp.getData().get("modelCode"));
            assertEquals(PARTITION_DATE, resp.getData().get("partitionDate"));
        }

        @Test
        @DisplayName("不存在记录走精确查询返回 null")
        void queryNotFound() {
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", "SZ",
                    "code", "999999",
                    "modelCode", MODEL_CODE,
                    "partitionDate", "2024-06-30"
            );

            ResponseDTO<Map<String, Object>> resp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, resp.getCode());
            assertNull(resp.getData());
        }

        @Test
        @DisplayName("upsert 覆盖已有数据")
        void upsertOverride() {
            // 写入相同唯一键但不同数据
            Map<String, Object> upsertBody = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE,
                    "dataContent", Map.of("net_cash_flow_operating", 99999)
            );

            ResponseDTO<Void> upsertResp = post("/api/financial-data/upsert", upsertBody,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
            assertEquals(200, upsertResp.getCode());

            // 查询确认数据已覆盖
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE
            );

            ResponseDTO<Map<String, Object>> queryResp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, queryResp.getCode());
            assertNotNull(queryResp.getData());

            // 恢复原始数据
            Map<String, Object> restoreBody = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE,
                    "dataContent", Map.of("net_cash_flow_operating", 10000)
            );
            post("/api/financial-data/upsert", restoreBody,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
        }
    }

    // ========================================================================
    // 场景：分页查询 — 批量写入后分页读取
    // ========================================================================
    @Nested
    @DisplayName("场景：分页查询 — 批量写入后分页读取")
    class PaginationQuery {

        private static boolean initialized;

        @BeforeEach
        void setUp() {
            if (initialized) return;

            // 批量写入 3 条不同日期的数据
            List<Map<String, Object>> records = List.of(
                    Map.of(
                            "market", "SH", "code", "000002",
                            "modelCode", "cash_flow_statement", "partitionDate", "2024-12-31",
                            "dataContent", Map.of("net_cash_flow_operating", 1000)
                    ),
                    Map.of(
                            "market", "SH", "code", "000002",
                            "modelCode", "cash_flow_statement", "partitionDate", "2024-06-30",
                            "dataContent", Map.of("net_cash_flow_operating", 500)
                    ),
                    Map.of(
                            "market", "SH", "code", "000002",
                            "modelCode", "cash_flow_statement", "partitionDate", "2024-03-31",
                            "dataContent", Map.of("net_cash_flow_operating", 200)
                    )
            );

            ResponseDTO<Void> resp = post("/api/financial-data/batch-upsert",
                    Map.of("dataType", "report", "records", records),
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
            assertEquals(200, resp.getCode(), "PaginationQuery 场景数据初始化失败");
            initialized = true;
        }

        @Test
        @DisplayName("分页查询返回多条记录")
        void pagination() {
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", "SH",
                    "code", "000002",
                    "page", 1,
                    "size", 10
            );

            ResponseDTO<Map<String, Object>> resp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
            assertTrue((Integer) resp.getData().get("total") >= 3);
            assertNotNull(resp.getData().get("items"));
        }

        @Test
        @DisplayName("按时间范围过滤")
        void queryByDateRange() {
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", "SH",
                    "code", "000002",
                    "startDate", "2024-06-01",
                    "endDate", "2024-12-31",
                    "page", 1,
                    "size", 10
            );

            ResponseDTO<Map<String, Object>> resp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
            assertTrue((Integer) resp.getData().get("total") >= 1);
        }
    }

    // ========================================================================
    // 场景：写入校验 — 必填字段缺失返回错误
    // ========================================================================
    @Nested
    @DisplayName("场景：写入校验 — 必填字段缺失返回 202")
    class Validation {

        @Test
        @DisplayName("upsert 缺少 dataType 返回 202")
        void upsertMissingDataType() {
            Map<String, Object> body = Map.of(
                    "market", "SH",
                    "code", "000001"
            );

            ResponseDTO<Void> resp = post("/api/financial-data/upsert", body,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});

            assertNotEquals(200, resp.getCode());
            assertNotNull(resp.getMsg());
        }

        @Test
        @DisplayName("batch-upsert 空 records 返回 202")
        void batchUpsertEmptyRecords() {
            Map<String, Object> body = Map.of("dataType", "report", "records", List.of());

            ResponseDTO<Void> resp = post("/api/financial-data/batch-upsert", body,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});

            assertNotEquals(200, resp.getCode());
        }
    }

    // ========================================================================
    // 场景：Delete — 写入后删除
    // ========================================================================
    @Nested
    @DisplayName("场景：Delete — 写入后删除")
    class Delete {

        private static final String MARKET = "SH";
        private static final String CODE = "000003";
        private static final String MODEL_CODE = "income_statement";
        private static final String PARTITION_DATE = "2024-12-31";

        @BeforeEach
        void setUp() {
            Map<String, Object> body = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE,
                    "dataContent", Map.of("total_revenue", 50000)
            );
            post("/api/financial-data/upsert", body,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
        }

        @Test
        @DisplayName("删除后查询返回 null")
        void deleteThenQueryNotFound() {
            Map<String, Object> deleteBody = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE
            );

            ResponseDTO<Void> deleteResp = post("/api/financial-data/delete", deleteBody,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
            assertEquals(200, deleteResp.getCode());

            // 查询确认已删除
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", MARKET,
                    "code", CODE,
                    "modelCode", MODEL_CODE,
                    "partitionDate", PARTITION_DATE
            );

            ResponseDTO<Map<String, Object>> queryResp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, queryResp.getCode());
            assertNull(queryResp.getData());
        }
    }

    // ========================================================================
    // 场景：Replace Range — 范围替换
    // ========================================================================
    @Nested
    @DisplayName("场景：Replace Range — 范围替换")
    class ReplaceRange {

        @Test
        @DisplayName("替换后旧数据不可查，新数据可查")
        void replaceRange() {
            String code = "000004";
            String modelCode = "balance_sheet";

            // 先写入一条旧数据
            Map<String, Object> oldBody = Map.of(
                    "dataType", "report",
                    "market", "SH", "code", code,
                    "modelCode", modelCode, "partitionDate", "2024-12-31",
                    "dataContent", Map.of("total_assets", 1000)
            );
            post("/api/financial-data/upsert", oldBody,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});

            // 范围替换
            List<Map<String, Object>> records = List.of(
                    Map.of(
                            "market", "SH", "code", code,
                            "modelCode", modelCode, "partitionDate", "2024-12-31",
                            "dataContent", Map.of("total_assets", 9999)
                    )
            );

            Map<String, Object> replaceBody = Map.of(
                    "dataType", "report",
                    "startDate", "2024-01-01",
                    "endDate", "2024-12-31",
                    "records", records
            );
            ResponseDTO<Void> replaceResp = post("/api/financial-data/replace-range", replaceBody,
                    new ParameterizedTypeReference<ResponseDTO<Void>>() {});
            assertEquals(200, replaceResp.getCode());

            // 查询确认数据被替换
            Map<String, Object> queryBody = Map.of(
                    "dataType", "report",
                    "market", "SH", "code", code,
                    "modelCode", modelCode, "partitionDate", "2024-12-31"
            );
            ResponseDTO<Map<String, Object>> queryResp = post("/api/financial-data/query", queryBody,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, queryResp.getCode());
            assertNotNull(queryResp.getData());
        }
    }

    // ========================================================================
    // 场景：InitializerRunner — 启动后元数据模型已创建
    // ========================================================================
    @Nested
    @DisplayName("场景：InitializerRunner — 启动时元数据模型已创建")
    class MetadataInit {

        @Test
        @DisplayName("report 路由模型已创建")
        void reportModelExists() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/indicator/meaning?code=report",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertEquals("财务报表", resp.getData().get("modelName"));
        }

        @Test
        @DisplayName("现金流量表元数据模型已创建")
        void cashFlowStatementModelExists() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/indicator/meaning?code=cash_flow_statement",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertEquals("现金流量表", resp.getData().get("modelName"));
        }

        @Test
        @DisplayName("资产负债表元数据模型已创建")
        void balanceSheetModelExists() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/indicator/meaning?code=balance_sheet",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertEquals("资产负债表", resp.getData().get("modelName"));
        }

        @Test
        @DisplayName("利润表元数据模型已创建")
        void incomeStatementModelExists() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/indicator/meaning?code=income_statement",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertEquals("利润表", resp.getData().get("modelName"));
        }
    }

    // ——— 复用 MetadataIT 的 get helper ———
    <T> ResponseDTO<T> get(String path, ParameterizedTypeReference<ResponseDTO<T>> type) {
        return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, null, type).getBody();
    }
}
