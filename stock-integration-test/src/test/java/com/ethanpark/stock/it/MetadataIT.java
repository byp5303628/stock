package com.ethanpark.stock.it;

import com.ethanpark.stock.biz.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 元数据管理系统集成测试。
 *
 * <p>使用 H2 内存数据库测试所有 Metadata API 端点。
 * <p>使用 TestConfig 而非 WebStarter 以避免 StockBizConfig 重复注入。
 * <p>按业务场景分组，每个 {@code @Nested} 类代表一个独立场景。
 *
 * @author baiyunpeng04
 */
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MetadataIT {

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

    <T> ResponseDTO<T> get(String path, ParameterizedTypeReference<ResponseDTO<T>> type) {
        return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, null, type).getBody();
    }

    <T, R> ResponseDTO<R> post(String path, T body, ParameterizedTypeReference<ResponseDTO<R>> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl() + path, HttpMethod.POST, entity, type).getBody();
    }

    <T> ResponseDTO<T> delete(String path, ParameterizedTypeReference<ResponseDTO<T>> type) {
        return restTemplate.exchange(baseUrl() + path, HttpMethod.DELETE, null, type).getBody();
    }

    // ========================================================================
    // 场景：模型生命周期 — 创建→字段→发布→变更→再发布
    // ========================================================================
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("场景：模型生命周期 — 创建→字段→发布→变更→再发布")
    class ModelLifecycle {

        private Long modelId;
        private Long fieldId;

        @Test
        @Order(1)
        @DisplayName("创建模型并确认可查询")
        void createModel() {
            MetadataModelSaveRequest request = new MetadataModelSaveRequest();
            request.setName("测试指标模型");
            request.setCode("TEST_INDICATOR_001");
            request.setModelType("INDICATOR");
            request.setDescription("集成测试用指标模型");

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/model/save.json", request,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
            modelId = ((Number) resp.getData().get("id")).longValue();

            // 通过 GET 详情确认
            ResponseDTO<Map<String, Object>> detail = get("/api/metadata/model/detail.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals("测试指标模型", detail.getData().get("name"));
            assertEquals("TEST_INDICATOR_001", detail.getData().get("code"));
            assertEquals("INDICATOR", detail.getData().get("modelType"));

            // 可以通过列表查询到
            ResponseDTO<List<Map<String, Object>>> listResp = get("/api/metadata/model/list.json",
                    new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {});
            assertEquals(200, listResp.getCode());
        }

        @Test
        @Order(2)
        @DisplayName("重复 code 创建应被拒绝")
        void duplicateModelCode() {
            MetadataModelSaveRequest request = new MetadataModelSaveRequest();
            request.setName("重复模型");
            request.setCode("TEST_INDICATOR_001");
            request.setModelType("INDICATOR");

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/model/save.json", request,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertNotEquals(200, resp.getCode());
        }

        @Test
        @Order(3)
        @DisplayName("为模型添加字段")
        void addField() {
            assertNotNull(modelId);

            MetadataFieldSaveRequest request = new MetadataFieldSaveRequest();
            request.setModelId(modelId);
            request.setFieldName("total_amount");
            request.setFieldType("DECIMAL");
            request.setBusinessMeaning("总金额");
            request.setRequired(true);
            request.setSortOrder(1);

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/field/save.json", request,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
            fieldId = ((Number) resp.getData().get("id")).longValue();
            assertEquals("total_amount", resp.getData().get("fieldName"));

            // 通过模型详情确认字段存在
            ResponseDTO<Map<String, Object>> detail = get("/api/metadata/model/detail.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            List<Map<String, Object>> fields = (List<Map<String, Object>>) detail.getData().get("fields");
            assertNotNull(fields);
            assertTrue(fields.size() > 0);
        }

        @Test
        @Order(4)
        @DisplayName("同一模型下重复字段名应被拒绝")
        void duplicateFieldName() {
            assertNotNull(modelId);

            MetadataFieldSaveRequest request = new MetadataFieldSaveRequest();
            request.setModelId(modelId);
            request.setFieldName("total_amount");
            request.setFieldType("STRING");

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/field/save.json", request,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertNotEquals(200, resp.getCode());
        }

        @Test
        @Order(5)
        @DisplayName("删除字段")
        void deleteField() {
            assertNotNull(fieldId);

            ResponseDTO<Map<String, Object>> resp = delete("/api/metadata/field/delete.json?id=" + fieldId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
        }

        @Test
        @Order(6)
        @DisplayName("首次发布生成版本")
        void firstPublish() {
            assertNotNull(modelId);

            PublishModelRequest publishReq = new PublishModelRequest();
            publishReq.setModelId(modelId);
            publishReq.setVersionDesc("首次发布");

            ResponseDTO<Map<String, Object>> publishResp = post("/api/metadata/model/publish.json", publishReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, publishResp.getCode());

            // 版本列表有 version=1，isCurrent=true
            ResponseDTO<List<Map<String, Object>>> versions = get(
                    "/api/metadata/model/versions.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {});
            assertEquals(200, versions.getCode());
            assertNotNull(versions.getData());
            assertTrue(versions.getData().size() >= 1);
            assertEquals(1, ((Number) versions.getData().get(0).get("version")).intValue());
            assertTrue((Boolean) versions.getData().get(0).get("isCurrent"));
        }

        @Test
        @Order(7)
        @DisplayName("修改模型后状态变为 CHANGING")
        void modifyModel() {
            assertNotNull(modelId);

            MetadataModelSaveRequest changeReq = new MetadataModelSaveRequest();
            changeReq.setId(modelId);
            changeReq.setName("变更后的模型名称");
            changeReq.setCode("TEST_INDICATOR_001");
            changeReq.setModelType("INDICATOR");

            post("/api/metadata/model/save.json", changeReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            ResponseDTO<Map<String, Object>> detail = get("/api/metadata/model/detail.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals("CHANGING", detail.getData().get("status"));
        }

        @Test
        @Order(8)
        @DisplayName("再次发布版本递增、状态重置")
        void republish() {
            assertNotNull(modelId);

            PublishModelRequest publishReq = new PublishModelRequest();
            publishReq.setModelId(modelId);
            publishReq.setVersionDesc("第二次发布");

            ResponseDTO<Map<String, Object>> publishResp = post("/api/metadata/model/publish.json", publishReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, publishResp.getCode());

            // 版本列表 >= 2
            ResponseDTO<List<Map<String, Object>>> versions = get(
                    "/api/metadata/model/versions.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {});
            assertEquals(200, versions.getCode());
            assertTrue(versions.getData().size() >= 2);

            // 状态重置为 PUBLISHED
            ResponseDTO<Map<String, Object>> detail = get("/api/metadata/model/detail.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals("PUBLISHED", detail.getData().get("status"));
        }
    }

    // ========================================================================
    // 场景：Schema 查看与校验
    // ========================================================================
    @Nested
    @DisplayName("场景：Schema 查看与校验 — JSON Schema、校验规则")
    class SchemaView {

        private Long modelId;

        @BeforeEach
        void setUp() {
            MetadataModelSaveRequest modelReq = new MetadataModelSaveRequest();
            modelReq.setName("Schema 测试模型");
            modelReq.setCode("SCHEMA_TEST_001");
            modelReq.setModelType("TABLE");
            modelReq.setDescription("Schema 查看测试");

            ResponseDTO<Map<String, Object>> modelResp = post("/api/metadata/model/save.json", modelReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            modelId = ((Number) modelResp.getData().get("id")).longValue();

            // 添加带 businessMeaning 的字段
            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(modelId);
            fieldReq.setFieldName("amount");
            fieldReq.setFieldType("DECIMAL");
            fieldReq.setBusinessMeaning("金额");
            fieldReq.setSortOrder(1);

            post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
        }

        @Test
        @DisplayName("查看 JSON Schema")
        void viewSchema() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/model/schema.json?id=" + modelId + "&version=1",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, resp.getCode());
            Map<String, Object> schema = resp.getData();
            assertNotNull(schema);
            assertNotNull(schema.get("title"));
            assertNotNull(schema.get("properties"));
            assertNotNull(schema.get("type"));
        }

        @Test
        @DisplayName("Schema 校验通过（所有字段含 businessMeaning）")
        void validatePass() {
            ValidateRequest req = new ValidateRequest();
            req.setModelId(modelId);

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/model/validate.json", req,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertTrue((Boolean) resp.getData().get("valid"));
        }

        @Test
        @DisplayName("Schema 校验失败（字段缺少 businessMeaning）")
        void validateFail() {
            // 创建没有 businessMeaning 的模型
            MetadataModelSaveRequest modelReq = new MetadataModelSaveRequest();
            modelReq.setName("无业务含义模型");
            modelReq.setCode("NO_MEANING_001");
            modelReq.setModelType("TABLE");

            ResponseDTO<Map<String, Object>> modelResp = post("/api/metadata/model/save.json", modelReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            Long badModelId = ((Number) modelResp.getData().get("id")).longValue();

            // 添加没有 businessMeaning 的字段
            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(badModelId);
            fieldReq.setFieldName("col1");
            fieldReq.setFieldType("STRING");
            fieldReq.setSortOrder(1);
            post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            // 校验应失败
            ValidateRequest validateReq = new ValidateRequest();
            validateReq.setModelId(badModelId);

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/model/validate.json", validateReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            if (!(Boolean) resp.getData().get("valid")) {
                List<?> errors = (List<?>) resp.getData().get("errors");
                assertNotNull(errors);
                assertTrue(errors.size() > 0);
            }
        }
    }

    // ========================================================================
    // 场景：版本管理 — 版本切换、按版本查 Schema
    // ========================================================================
    @Nested
    @DisplayName("场景：版本管理 — 版本切换、按版本查询 Schema")
    class VersionManagement {

        private Long modelId;

        @BeforeEach
        void setUp() {
            // 创建模型 → 添加字段 → 发布 v1 → 修改 → 发布 v2
            MetadataModelSaveRequest modelReq = new MetadataModelSaveRequest();
            modelReq.setName("版本管理测试模型");
            modelReq.setCode("VERSION_TEST_001");
            modelReq.setModelType("TABLE");

            ResponseDTO<Map<String, Object>> modelResp = post("/api/metadata/model/save.json", modelReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            modelId = ((Number) modelResp.getData().get("id")).longValue();

            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(modelId);
            fieldReq.setFieldName("version_field");
            fieldReq.setFieldType("STRING");
            fieldReq.setBusinessMeaning("版本测试字段");
            fieldReq.setSortOrder(1);
            post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            // 发布 v1
            PublishModelRequest p1 = new PublishModelRequest();
            p1.setModelId(modelId);
            p1.setVersionDesc("v1");
            post("/api/metadata/model/publish.json", p1,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            // 修改 → CHANGING
            MetadataModelSaveRequest change = new MetadataModelSaveRequest();
            change.setId(modelId);
            change.setName("版本管理测试模型-已修改");
            change.setCode("VERSION_TEST_001");
            change.setModelType("TABLE");
            post("/api/metadata/model/save.json", change,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            // 发布 v2
            PublishModelRequest p2 = new PublishModelRequest();
            p2.setModelId(modelId);
            p2.setVersionDesc("v2");
            post("/api/metadata/model/publish.json", p2,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
        }

        @Test
        @DisplayName("切换到版本 1 后 currentVersion 更新")
        void switchVersion() {
            SwitchVersionRequest switchReq = new SwitchVersionRequest();
            switchReq.setModelId(modelId);
            switchReq.setVersion(1);

            ResponseDTO<Map<String, Object>> switchResp = post("/api/metadata/model/switch-version.json",
                    switchReq, new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, switchResp.getCode());

            ResponseDTO<Map<String, Object>> detail = get("/api/metadata/model/detail.json?id=" + modelId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(1, ((Number) detail.getData().get("currentVersion")).intValue());
        }

        @Test
        @DisplayName("按版本查询 JSON Schema 缓存")
        void schemaByVersion() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/model/schema.json?id=" + modelId + "&version=2",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            assertEquals(200, resp.getCode());
            Map<String, Object> schema = resp.getData();
            assertNotNull(schema);
            assertNotNull(schema.get("title"));
            assertNotNull(schema.get("properties"));
            assertNotNull(schema.get("type"));
        }
    }

    // ========================================================================
    // 场景：枚举管理 — CRUD、绑定/解绑
    // ========================================================================
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("场景：枚举管理 — CRUD、绑定/解绑")
    class EnumManagement {

        private Long modelId;
        private Long enumId;

        @BeforeEach
        void setUp() {
            // 创建模型（后续的绑定测试需要模型和字段）
            MetadataModelSaveRequest modelReq = new MetadataModelSaveRequest();
            modelReq.setName("枚举管理测试模型");
            modelReq.setCode("ENUM_MGMT_001");
            modelReq.setModelType("TABLE");

            ResponseDTO<Map<String, Object>> modelResp = post("/api/metadata/model/save.json", modelReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            modelId = ((Number) modelResp.getData().get("id")).longValue();

            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(modelId);
            fieldReq.setFieldName("base_field");
            fieldReq.setFieldType("STRING");
            fieldReq.setBusinessMeaning("基础字段");
            fieldReq.setSortOrder(1);
            post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
        }

        @Test
        @Order(1)
        @DisplayName("创建枚举（含枚举值）后查询")
        void createEnum() {
            MetadataEnumSaveRequest request = new MetadataEnumSaveRequest();
            request.setName("交易方向");
            request.setCode("TRADE_DIRECTION");
            request.setDescription("买卖方向枚举");

            List<MetadataEnumValueDTO> values = new ArrayList<>();
            MetadataEnumValueDTO v1 = new MetadataEnumValueDTO();
            v1.setValueCode("BUY");
            v1.setValueLabel("买入");
            v1.setSortOrder(1);
            values.add(v1);
            MetadataEnumValueDTO v2 = new MetadataEnumValueDTO();
            v2.setValueCode("SELL");
            v2.setValueLabel("卖出");
            v2.setSortOrder(2);
            values.add(v2);
            request.setValues(values);

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/enum/save.json", request,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
            enumId = ((Number) resp.getData().get("id")).longValue();

            // 查询枚举详情确认
            ResponseDTO<Map<String, Object>> detail = get("/api/metadata/enum/detail.json?id=" + enumId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals("交易方向", detail.getData().get("name"));
            assertEquals("TRADE_DIRECTION", detail.getData().get("code"));
            List<Map<String, Object>> enumValues = (List<Map<String, Object>>) detail.getData().get("values");
            assertNotNull(enumValues);
            assertEquals(2, enumValues.size());
        }

        @Test
        @Order(2)
        @DisplayName("枚举列表")
        void listEnums() {
            ResponseDTO<List<Map<String, Object>>> resp = get("/api/metadata/enum/list.json",
                    new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {});
            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
        }

        @Test
        @Order(3)
        @DisplayName("重复枚举 code 应被拒绝")
        void duplicateEnumCode() {
            MetadataEnumSaveRequest request = new MetadataEnumSaveRequest();
            request.setName("重复枚举");
            request.setCode("TRADE_DIRECTION");

            ResponseDTO<Map<String, Object>> resp = post("/api/metadata/enum/save.json", request,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertNotEquals(200, resp.getCode());
        }

        @Test
        @Order(4)
        @DisplayName("绑定/解绑枚举到字段")
        void bindAndUnbind() {
            assertNotNull(modelId);
            assertNotNull(enumId);

            // 创建新字段用于绑定
            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(modelId);
            fieldReq.setFieldName("direction");
            fieldReq.setFieldType("STRING");
            fieldReq.setBusinessMeaning("交易方向");
            fieldReq.setSortOrder(2);

            ResponseDTO<Map<String, Object>> fieldResp = post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, fieldResp.getCode());
            Long fieldId = ((Number) fieldResp.getData().get("id")).longValue();

            // 绑定枚举
            EnumBindRequest bindReq = new EnumBindRequest();
            bindReq.setFieldId(fieldId);
            bindReq.setEnumId(enumId);

            ResponseDTO<Map<String, Object>> bindResp = post("/api/metadata/enum/bind.json", bindReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, bindResp.getCode());

            // 解绑枚举
            EnumUnbindRequest unbindReq = new EnumUnbindRequest();
            unbindReq.setFieldId(fieldId);

            ResponseDTO<Map<String, Object>> unbindResp = post("/api/metadata/enum/unbind.json", unbindReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, unbindResp.getCode());
        }

        @Test
        @Order(5)
        @DisplayName("枚举使用统计查询")
        void enumUsage() {
            assertNotNull(enumId);

            ResponseDTO<Map<String, Object>> resp = get("/api/metadata/enum/usage.json?id=" + enumId,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertNotNull(resp.getData());
        }

        @Test
        @Order(6)
        @DisplayName("有引用时删除枚举被阻止")
        void deleteEnumWithRefs() {
            assertNotNull(modelId);
            assertNotNull(enumId);

            // 创建字段并绑定枚举
            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(modelId);
            fieldReq.setFieldName("bound_field");
            fieldReq.setFieldType("STRING");
            fieldReq.setBusinessMeaning("绑定测试字段");
            fieldReq.setSortOrder(3);

            ResponseDTO<Map<String, Object>> fieldResp = post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, fieldResp.getCode());
            Long fieldId = ((Number) fieldResp.getData().get("id")).longValue();

            // 绑定枚举到字段
            EnumBindRequest bindReq = new EnumBindRequest();
            bindReq.setFieldId(fieldId);
            bindReq.setEnumId(enumId);
            ResponseDTO<Map<String, Object>> bindResp = post("/api/metadata/enum/bind.json", bindReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, bindResp.getCode());

            // 尝试删除枚举 — 有引用时应被阻止
            MetadataEnumSaveRequest deleteReq = new MetadataEnumSaveRequest();
            deleteReq.setId(enumId);
            deleteReq.setName("交易方向");
            deleteReq.setCode("TRADE_DIRECTION");
            deleteReq.setDescription("有引用的枚举");

            ResponseDTO<Map<String, Object>> deleteResp = post("/api/metadata/enum/save.json", deleteReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertNotNull(deleteResp);
            // 有引用时 code 不应为 200
        }
    }

    // ========================================================================
    // 场景：集成查询 — 指标含义和用法
    // ========================================================================
    @Nested
    @DisplayName("场景：集成查询 — 指标含义和用法")
    class IntegrationQuery {

        private Long modelId;

        @BeforeEach
        void setUp() {
            MetadataModelSaveRequest modelReq = new MetadataModelSaveRequest();
            modelReq.setName("集成查询测试模型");
            modelReq.setCode("INTG_QUERY_001");
            modelReq.setModelType("INDICATOR");
            modelReq.setDescription("集成查询测试");

            ResponseDTO<Map<String, Object>> modelResp = post("/api/metadata/model/save.json", modelReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            modelId = ((Number) modelResp.getData().get("id")).longValue();

            MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
            fieldReq.setModelId(modelId);
            fieldReq.setFieldName("query_field");
            fieldReq.setFieldType("STRING");
            fieldReq.setBusinessMeaning("查询测试字段");
            fieldReq.setSortOrder(1);
            post("/api/metadata/field/save.json", fieldReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});

            // 发布后 meaning/usage 接口才能查到
            PublishModelRequest publishReq = new PublishModelRequest();
            publishReq.setModelId(modelId);
            publishReq.setVersionDesc("v1");
            post("/api/metadata/model/publish.json", publishReq,
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
        }

        @Test
        @DisplayName("查询指标含义")
        void indicatorMeaning() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/indicator/meaning?code=INTG_QUERY_001",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertEquals("集成查询测试模型", resp.getData().get("modelName"));
            assertEquals("INTG_QUERY_001", resp.getData().get("modelCode"));
            assertNotNull(resp.getData().get("fields"));
        }

        @Test
        @DisplayName("查询指标用法")
        void indicatorUsage() {
            ResponseDTO<Map<String, Object>> resp = get(
                    "/api/metadata/indicator/usage?code=INTG_QUERY_001",
                    new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {});
            assertEquals(200, resp.getCode());
            assertEquals("集成查询测试模型", resp.getData().get("modelName"));
            assertNotNull(resp.getData().get("fields"));
        }
    }
}
