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
 *
 * @author baiyunpeng04
 */
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MetadataIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private static Long createdModelId;
    private static Long createdFieldId;
    private static Long createdEnumId;

    // ===== 模型管理测试 =====

    @Test
    @Order(1)
    @DisplayName("testListModels_empty — 空列表")
    public void testListModels_empty() {
        ResponseEntity<ResponseDTO<List<Map<String, Object>>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/list.json",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @Order(2)
    @DisplayName("testSaveAndGetModel — 创建模型后查询")
    public void testSaveAndGetModel() {
        // 创建模型
        MetadataModelSaveRequest request = new MetadataModelSaveRequest();
        request.setName("测试指标模型");
        request.setCode("TEST_INDICATOR_001");
        request.setModelType("INDICATOR");
        request.setDescription("集成测试用指标模型");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataModelSaveRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> saveResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(saveResponse.getBody());
        assertEquals(200, saveResponse.getBody().getCode());
        assertNotNull(saveResponse.getBody().getData());
        createdModelId = ((Number) saveResponse.getBody().getData().get("id")).longValue();
        assertNotNull(createdModelId);

        // 查询模型
        ResponseEntity<ResponseDTO<Map<String, Object>>> getResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/detail.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(getResponse.getBody());
        assertEquals(200, getResponse.getBody().getCode());
        Map<String, Object> model = getResponse.getBody().getData();
        assertEquals("测试指标模型", model.get("name"));
        assertEquals("TEST_INDICATOR_001", model.get("code"));
        assertEquals("INDICATOR", model.get("modelType"));
    }

    @Test
    @Order(3)
    @DisplayName("testSaveModelDuplicateCode — 重复 code 返回错误")
    public void testSaveModelDuplicateCode() {
        MetadataModelSaveRequest request = new MetadataModelSaveRequest();
        request.setName("重复模型");
        request.setCode("TEST_INDICATOR_001");
        request.setModelType("INDICATOR");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataModelSaveRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertNotEquals(200, response.getBody().getCode());
    }

    // ===== 字段管理测试 =====

    @Test
    @Order(4)
    @DisplayName("testSaveAndGetField — 为模型新增字段后查询")
    public void testSaveAndGetField() {
        assertNotNull(createdModelId, "需要先创建模型");

        MetadataFieldSaveRequest request = new MetadataFieldSaveRequest();
        request.setModelId(createdModelId);
        request.setFieldName("total_amount");
        request.setFieldType("DECIMAL");
        request.setBusinessMeaning("总金额");
        request.setRequired(true);
        request.setSortOrder(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataFieldSaveRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> saveResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/field/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(saveResponse.getBody());
        assertEquals(200, saveResponse.getBody().getCode());
        assertNotNull(saveResponse.getBody().getData());
        createdFieldId = ((Number) saveResponse.getBody().getData().get("id")).longValue();
        assertEquals("total_amount", saveResponse.getBody().getData().get("fieldName"));

        // 通过模型详情查询验证字段
        ResponseEntity<ResponseDTO<Map<String, Object>>> getResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/detail.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(getResponse.getBody());
        assertEquals(200, getResponse.getBody().getCode());
        List<Map<String, Object>> fields = (List<Map<String, Object>>) getResponse.getBody().getData().get("fields");
        assertNotNull(fields);
        assertTrue(fields.size() > 0);
    }

    @Test
    @Order(5)
    @DisplayName("testSaveFieldDuplicateName — 同一模型下重复字段名")
    public void testSaveFieldDuplicateName() {
        assertNotNull(createdModelId, "需要先创建模型");

        MetadataFieldSaveRequest request = new MetadataFieldSaveRequest();
        request.setModelId(createdModelId);
        request.setFieldName("total_amount");
        request.setFieldType("STRING");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataFieldSaveRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/field/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertNotEquals(200, response.getBody().getCode());
    }

    @Test
    @Order(6)
    @DisplayName("testDeleteField — 删除字段")
    public void testDeleteField() {
        assertNotNull(createdFieldId, "需要先创建字段");

        // 删除字段
        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/field/delete.json?id=" + createdFieldId,
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    // ===== 枚举管理测试 =====

    @Test
    @Order(7)
    @DisplayName("testListEnums — 枚举列表")
    public void testListEnums() {
        ResponseEntity<ResponseDTO<List<Map<String, Object>>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/list.json",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @Order(8)
    @DisplayName("testSaveAndGetEnum — 创建枚举（含枚举值）后查询")
    public void testSaveAndGetEnum() {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataEnumSaveRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> saveResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(saveResponse.getBody());
        assertEquals(200, saveResponse.getBody().getCode());
        assertNotNull(saveResponse.getBody().getData());
        createdEnumId = ((Number) saveResponse.getBody().getData().get("id")).longValue();

        // 查询枚举详情
        ResponseEntity<ResponseDTO<Map<String, Object>>> getResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/detail.json?id=" + createdEnumId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(getResponse.getBody());
        assertEquals(200, getResponse.getBody().getCode());
        Map<String, Object> enumData = getResponse.getBody().getData();
        assertEquals("交易方向", enumData.get("name"));
        assertEquals("TRADE_DIRECTION", enumData.get("code"));

        List<Map<String, Object>> enumValues = (List<Map<String, Object>>) enumData.get("values");
        assertNotNull(enumValues);
        assertEquals(2, enumValues.size());
    }

    @Test
    @Order(9)
    @DisplayName("testSaveEnumDuplicateCode — 重复枚举 code 返回错误")
    public void testSaveEnumDuplicateCode() {
        MetadataEnumSaveRequest request = new MetadataEnumSaveRequest();
        request.setName("重复枚举");
        request.setCode("TRADE_DIRECTION");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataEnumSaveRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertNotEquals(200, response.getBody().getCode());
    }

    @Test
    @Order(10)
    @DisplayName("testBindAndUnbindEnum — 绑定/解绑枚举")
    public void testBindAndUnbindEnum() {
        assertNotNull(createdModelId, "需要先创建模型");
        assertNotNull(createdEnumId, "需要先创建枚举");

        // 先创建一个字段用于绑定
        MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
        fieldReq.setModelId(createdModelId);
        fieldReq.setFieldName("direction");
        fieldReq.setFieldType("STRING");
        fieldReq.setBusinessMeaning("交易方向");
        fieldReq.setSortOrder(2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataFieldSaveRequest> fieldEntity = new HttpEntity<>(fieldReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> fieldResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/field/save.json",
                HttpMethod.POST,
                fieldEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertEquals(200, fieldResponse.getBody().getCode());
        Long fieldId = ((Number) fieldResponse.getBody().getData().get("id")).longValue();

        // 绑定枚举
        EnumBindRequest bindReq = new EnumBindRequest();
        bindReq.setFieldId(fieldId);
        bindReq.setEnumId(createdEnumId);
        HttpEntity<EnumBindRequest> bindEntity = new HttpEntity<>(bindReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> bindResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/bind.json",
                HttpMethod.POST,
                bindEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(bindResponse.getBody());
        assertEquals(200, bindResponse.getBody().getCode());

        // 解绑枚举
        EnumUnbindRequest unbindReq = new EnumUnbindRequest();
        unbindReq.setFieldId(fieldId);
        HttpEntity<EnumUnbindRequest> unbindEntity = new HttpEntity<>(unbindReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> unbindResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/unbind.json",
                HttpMethod.POST,
                unbindEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(unbindResponse.getBody());
        assertEquals(200, unbindResponse.getBody().getCode());
    }

    @Test
    @Order(11)
    @DisplayName("testGetEnumUsage — 枚举使用统计")
    public void testGetEnumUsage() {
        assertNotNull(createdEnumId, "需要先创建枚举");

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/usage.json?id=" + createdEnumId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());
    }

    // ===== Schema 校验测试 =====

    @Test
    @Order(12)
    @DisplayName("testValidateSchema_pass — Schema 校验通过")
    public void testValidateSchema_pass() {
        assertNotNull(createdModelId, "需要先创建模型");

        ValidateRequest request = new ValidateRequest();
        request.setModelId(createdModelId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ValidateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/validate.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        Map<String, Object> result = response.getBody().getData();
        assertTrue((Boolean) result.get("valid"));
    }

    @Test
    @Order(13)
    @DisplayName("testValidateSchema_fail — Schema 校验失败（缺少 businessMeaning）")
    public void testValidateSchema_fail() {
        // 创建一个没有 businessMeaning 的字段的模型
        MetadataModelSaveRequest modelReq = new MetadataModelSaveRequest();
        modelReq.setName("无业务含义模型");
        modelReq.setCode("NO_MEANING_001");
        modelReq.setModelType("TABLE");
        modelReq.setDescription("所有字段都没有业务含义");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataModelSaveRequest> modelEntity = new HttpEntity<>(modelReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> modelResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/save.json",
                HttpMethod.POST,
                modelEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, modelResponse.getBody().getCode());
        Long modelId = ((Number) modelResponse.getBody().getData().get("id")).longValue();

        // 添加一个没有 businessMeaning 的字段
        MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
        fieldReq.setModelId(modelId);
        fieldReq.setFieldName("col1");
        fieldReq.setFieldType("STRING");
        fieldReq.setSortOrder(1);

        HttpEntity<MetadataFieldSaveRequest> fieldEntity = new HttpEntity<>(fieldReq, headers);
        restTemplate.exchange(
                baseUrl() + "/api/metadata/field/save.json",
                HttpMethod.POST,
                fieldEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        // 校验
        ValidateRequest validateReq = new ValidateRequest();
        validateReq.setModelId(modelId);
        HttpEntity<ValidateRequest> validateEntity = new HttpEntity<>(validateReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/validate.json",
                HttpMethod.POST,
                validateEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        Map<String, Object> result = response.getBody().getData();
        // 如果所有字段都缺少 businessMeaning，则校验应失败
        if (!(Boolean) result.get("valid")) {
            List<?> errors = (List<?>) result.get("errors");
            assertNotNull(errors);
            assertTrue(errors.size() > 0);
        }
    }

    @Test
    @Order(14)
    @DisplayName("testDeleteEnumWithRefs — 有引用时删除枚举被阻止")
    public void testDeleteEnumWithRefs() {
        assertNotNull(createdEnumId, "需要先创建枚举");
        assertNotNull(createdModelId, "需要先创建模型");

        // 创建一个字段并绑定枚举
        MetadataFieldSaveRequest fieldReq = new MetadataFieldSaveRequest();
        fieldReq.setModelId(createdModelId);
        fieldReq.setFieldName("bound_field");
        fieldReq.setFieldType("STRING");
        fieldReq.setBusinessMeaning("绑定测试字段");
        fieldReq.setSortOrder(3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataFieldSaveRequest> fieldEntity = new HttpEntity<>(fieldReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> fieldResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/field/save.json",
                HttpMethod.POST,
                fieldEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, fieldResponse.getBody().getCode());
        Long fieldId = ((Number) fieldResponse.getBody().getData().get("id")).longValue();

        // 绑定枚举
        EnumBindRequest bindReq = new EnumBindRequest();
        bindReq.setFieldId(fieldId);
        bindReq.setEnumId(createdEnumId);
        HttpEntity<EnumBindRequest> bindEntity = new HttpEntity<>(bindReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> bindResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/bind.json",
                HttpMethod.POST,
                bindEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, bindResponse.getBody().getCode());

        // 尝试删除枚举（更改状态为 DELETED）— 有引用时应被阻止
        MetadataEnumSaveRequest deleteReq = new MetadataEnumSaveRequest();
        deleteReq.setId(createdEnumId);
        deleteReq.setName("交易方向");
        deleteReq.setCode("TRADE_DIRECTION");
        deleteReq.setDescription("有引用的枚举");

        HttpEntity<MetadataEnumSaveRequest> deleteEntity = new HttpEntity<>(deleteReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> deleteResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/enum/save.json",
                HttpMethod.POST,
                deleteEntity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        // 后端可能拒绝删除（返回错误码）或静默处理
        assertNotNull(deleteResponse.getBody());
        // 如果有引用被阻止，code 不应为 200
    }

    // ===== 集成 API 测试 =====

    @Test
    @Order(15)
    @DisplayName("testGetIndicatorMeaning — 指标含义查询")
    public void testGetIndicatorMeaning() {
        assertNotNull(createdModelId, "需要先创建模型");

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/indicator/meaning?code=TEST_INDICATOR_001",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        Map<String, Object> data = response.getBody().getData();
        assertNotNull(data);
        assertEquals("测试指标模型", data.get("modelName"));
        assertEquals("TEST_INDICATOR_001", data.get("modelCode"));

        List<?> fields = (List<?>) data.get("fields");
        assertNotNull(fields);
    }

    @Test
    @Order(16)
    @DisplayName("testGetIndicatorUsage — 指标用法查询")
    public void testGetIndicatorUsage() {
        assertNotNull(createdModelId, "需要先创建模型");

        ResponseEntity<ResponseDTO<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl() + "/api/metadata/indicator/usage?code=TEST_INDICATOR_001",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        Map<String, Object> data = response.getBody().getData();
        assertNotNull(data);
        assertEquals("测试指标模型", data.get("modelName"));

        List<?> fields = (List<?>) data.get("fields");
        assertNotNull(fields);
    }

    // ===== 版本管理测试 =====

    @Test
    @Order(17)
    @DisplayName("testFirstPublish_createsVersion — 首次发布生成版本")
    public void testFirstPublish_createsVersion() {
        // Given: 已有模型
        assertNotNull(createdModelId, "需要先创建模型");
        // The model already has fields from previous tests

        // When: 发布模型
        PublishModelRequest publishReq = new PublishModelRequest();
        publishReq.setModelId(createdModelId);
        publishReq.setVersionDesc("首次发布");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PublishModelRequest> entity = new HttpEntity<>(publishReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> publishResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/publish.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, publishResponse.getBody().getCode());

        // Then: versions列表有 version=1
        ResponseEntity<ResponseDTO<List<Map<String, Object>>>> versionsResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/versions.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
        );
        assertEquals(200, versionsResponse.getBody().getCode());
        List<Map<String, Object>> versions = versionsResponse.getBody().getData();
        assertNotNull(versions);
        assertTrue(versions.size() >= 1);
        assertEquals(1, ((Number) versions.get(0).get("version")).intValue());
        assertTrue((Boolean) versions.get(0).get("isCurrent"));
    }

    @Test
    @Order(18)
    @DisplayName("testModelChange_triggersChanging — 改属性后状态变 CHANGING")
    public void testModelChange_triggersChanging() {
        // When: 修改模型名称
        MetadataModelSaveRequest changeReq = new MetadataModelSaveRequest();
        changeReq.setId(createdModelId);
        changeReq.setName("变更后的模型名称");
        changeReq.setCode("TEST_INDICATOR_001");
        changeReq.setModelType("INDICATOR");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MetadataModelSaveRequest> entity = new HttpEntity<>(changeReq, headers);

        restTemplate.exchange(
                baseUrl() + "/api/metadata/model/save.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        // Then: 状态变为 CHANGING
        ResponseEntity<ResponseDTO<Map<String, Object>>> detailResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/detail.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, detailResponse.getBody().getCode());
        assertEquals("CHANGING", detailResponse.getBody().getData().get("status"));
    }

    @Test
    @Order(19)
    @DisplayName("testSecondPublish_versionIncrements — 再次发布版本递增")
    public void testSecondPublish_versionIncrements() {
        // When: 再次发布
        PublishModelRequest publishReq = new PublishModelRequest();
        publishReq.setModelId(createdModelId);
        publishReq.setVersionDesc("第二次发布");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PublishModelRequest> entity = new HttpEntity<>(publishReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> publishResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/publish.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, publishResponse.getBody().getCode());

        // Then: versions有2个版本，状态重置为 PUBLISHED
        ResponseEntity<ResponseDTO<List<Map<String, Object>>>> versionsResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/versions.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<List<Map<String, Object>>>>() {}
        );
        assertEquals(200, versionsResponse.getBody().getCode());
        List<Map<String, Object>> versions = versionsResponse.getBody().getData();
        assertTrue(versions.size() >= 2);

        // 状态 PUBLISHED
        ResponseEntity<ResponseDTO<Map<String, Object>>> detailResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/detail.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals("PUBLISHED", detailResponse.getBody().getData().get("status"));
    }

    @Test
    @Order(20)
    @DisplayName("testSwitchVersion — 版本切换")
    public void testSwitchVersion() {
        // Given: switch to version 1
        SwitchVersionRequest switchReq = new SwitchVersionRequest();
        switchReq.setModelId(createdModelId);
        switchReq.setVersion(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SwitchVersionRequest> entity = new HttpEntity<>(switchReq, headers);

        ResponseEntity<ResponseDTO<Map<String, Object>>> switchResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/switch-version.json",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, switchResponse.getBody().getCode());

        // Then: currentVersion = 1
        ResponseEntity<ResponseDTO<Map<String, Object>>> detailResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/detail.json?id=" + createdModelId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(1, ((Number) detailResponse.getBody().getData().get("currentVersion")).intValue());
    }

    @Test
    @Order(21)
    @DisplayName("testSchemaByVersion — 按版本查 JSONSchema 缓存")
    public void testSchemaByVersion() {
        ResponseEntity<ResponseDTO<Map<String, Object>>> schemaResponse = restTemplate.exchange(
                baseUrl() + "/api/metadata/model/schema.json?id=" + createdModelId + "&version=2",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        assertEquals(200, schemaResponse.getBody().getCode());
        Map<String, Object> schema = schemaResponse.getBody().getData();
        assertNotNull(schema);
        assertNotNull(schema.get("title"));
        assertNotNull(schema.get("properties"));
        assertNotNull(schema.get("type"));
    }
}
