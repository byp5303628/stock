package com.ethanpark.stock.core.converter;

import com.ethanpark.stock.common.dal.mappers.entity.*;
import com.ethanpark.stock.core.model.metadata.MetadataEnum;
import com.ethanpark.stock.core.model.metadata.MetadataEnumValue;
import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbConverter 单元测试 — 重点覆盖 NOT NULL + DEFAULT 列的 null-safe 默认值。
 *
 * @author baiyunpeng04
 */
public class DbConverterTest {

    // ==================== MetadataModel ====================

    @Test
    @DisplayName("toDbEntity MetadataModel: null 输入返回 null")
    public void toDbEntity_model_nullInput_returnsNull() {
        assertNull(DbConverter.toDbEntity((MetadataModel) null));
    }

    @Test
    @DisplayName("toDbEntity MetadataModel: status 为 null 时默认 DRAFT")
    public void toDbEntity_model_nullStatus_defaultsToDraft() {
        MetadataModel domain = new MetadataModel();
        domain.setName("测试模型");
        domain.setCode("TEST_001");
        domain.setModelType("indicator");

        MetadataModelDO result = DbConverter.toDbEntity(domain);

        assertEquals("DRAFT", result.getStatus(),
                "status 为 null 时应默认 'DRAFT'，与 DB schema DEFAULT 'DRAFT' 对齐");
    }

    @Test
    @DisplayName("toDbEntity MetadataModel: status 有值时保留原值")
    public void toDbEntity_model_providedStatus_preserved() {
        MetadataModel domain = new MetadataModel();
        domain.setName("已发布模型");
        domain.setCode("PUB_001");
        domain.setModelType("indicator");
        domain.setStatus("PUBLISHED");

        MetadataModelDO result = DbConverter.toDbEntity(domain);

        assertEquals("PUBLISHED", result.getStatus(), "明确传入 status 时应保留原值");
    }

    @Test
    @DisplayName("toDbEntity MetadataModel: 正常字段全部传递")
    public void toDbEntity_model_allFieldsPreserved() {
        MetadataModel domain = new MetadataModel();
        domain.setId(1L);
        domain.setName("完整模型");
        domain.setCode("FULL_001");
        domain.setModelType("indicator");
        domain.setDescription("完整的描述信息");
        domain.setStatus("DRAFT");

        MetadataModelDO result = DbConverter.toDbEntity(domain);

        assertEquals(1L, result.getId());
        assertEquals("完整模型", result.getName());
        assertEquals("FULL_001", result.getCode());
        assertEquals("indicator", result.getModelType());
        assertEquals("完整的描述信息", result.getDescription());
        assertEquals("DRAFT", result.getStatus());
        assertNotNull(result.getGmtCreate(), "gmtCreate 应自动填充");
        assertNotNull(result.getGmtModified(), "gmtModified 应自动填充");
    }

    // ==================== MetadataEnum ====================

    @Test
    @DisplayName("toDbEntity MetadataEnum: null 输入返回 null")
    public void toDbEntity_enum_nullInput_returnsNull() {
        assertNull(DbConverter.toDbEntity((MetadataEnum) null));
    }

    @Test
    @DisplayName("toDbEntity MetadataEnum: status 为 null 时默认 ENABLED")
    public void toDbEntity_enum_nullStatus_defaultsToEnabled() {
        MetadataEnum domain = new MetadataEnum();
        domain.setName("测试枚举");
        domain.setCode("TEST_ENUM");

        MetadataEnumDO result = DbConverter.toDbEntity(domain);

        assertEquals("ENABLED", result.getStatus(),
                "status 为 null 时应默认 'ENABLED'，与 DB schema DEFAULT 'ENABLED' 对齐");
    }

    @Test
    @DisplayName("toDbEntity MetadataEnum: status 有值时保留原值")
    public void toDbEntity_enum_providedStatus_preserved() {
        MetadataEnum domain = new MetadataEnum();
        domain.setName("已禁用枚举");
        domain.setCode("DISABLED_ENUM");
        domain.setStatus("DISABLED");

        MetadataEnumDO result = DbConverter.toDbEntity(domain);

        assertEquals("DISABLED", result.getStatus(), "明确传入 status 时应保留原值");
    }

    // ==================== MetadataField ====================

    @Test
    @DisplayName("toDbEntity MetadataField: null 输入返回 null")
    public void toDbEntity_field_nullInput_returnsNull() {
        assertNull(DbConverter.toDbEntity((MetadataField) null));
    }

    @Test
    @DisplayName("toDbEntity MetadataField: businessMeaning 为 null 时默认空字符串")
    public void toDbEntity_field_nullBusinessMeaning_defaultsToEmpty() {
        MetadataField domain = new MetadataField();
        domain.setModelId(1L);
        domain.setFieldName("test_field");
        domain.setFieldType("STRING");

        MetadataFieldDO result = DbConverter.toDbEntity(domain);

        assertEquals("", result.getBusinessMeaning(),
                "businessMeaning 为 null 时应默认 ''，与 DB schema DEFAULT '' 对齐");
    }

    @Test
    @DisplayName("toDbEntity MetadataField: sortOrder 为 null 时默认 0")
    public void toDbEntity_field_nullSortOrder_defaultsToZero() {
        MetadataField domain = new MetadataField();
        domain.setModelId(1L);
        domain.setFieldName("test_field");
        domain.setFieldType("STRING");
        domain.setBusinessMeaning("有含义");

        MetadataFieldDO result = DbConverter.toDbEntity(domain);

        assertEquals(0, result.getSortOrder(),
                "sortOrder 为 null 时应默认 0，与 DB schema DEFAULT 0 对齐");
    }

    @Test
    @DisplayName("toDbEntity MetadataField: required 为 null 时默认 0")
    public void toDbEntity_field_nullRequired_defaultsToZero() {
        MetadataField domain = new MetadataField();
        domain.setModelId(1L);
        domain.setFieldName("optional_field");
        domain.setFieldType("STRING");
        domain.setRequired(null);

        MetadataFieldDO result = DbConverter.toDbEntity(domain);

        assertEquals(0, result.getRequired(), "required 为 null 时应默认 0");
    }

    @Test
    @DisplayName("toDbEntity MetadataField: required 为 true 时映射为 1")
    public void toDbEntity_field_requiredTrue_mapsToOne() {
        MetadataField domain = new MetadataField();
        domain.setModelId(1L);
        domain.setFieldName("required_field");
        domain.setFieldType("STRING");
        domain.setRequired(true);

        MetadataFieldDO result = DbConverter.toDbEntity(domain);

        assertEquals(1, result.getRequired(), "required=true 时应映射为 1");
    }

    @Test
    @DisplayName("toDbEntity MetadataField: 正常字段全部传递")
    public void toDbEntity_field_allFieldsPreserved() {
        MetadataField domain = new MetadataField();
        domain.setId(10L);
        domain.setModelId(1L);
        domain.setFieldName("full_field");
        domain.setFieldType("ENUM");
        domain.setBusinessMeaning("完整的业务含义");
        domain.setRequired(true);
        domain.setEnumId(5L);
        domain.setSortOrder(99);

        MetadataFieldDO result = DbConverter.toDbEntity(domain);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getModelId());
        assertEquals("full_field", result.getFieldName());
        assertEquals("ENUM", result.getFieldType());
        assertEquals("完整的业务含义", result.getBusinessMeaning());
        assertEquals(1, result.getRequired());
        assertEquals(Long.valueOf(5L), result.getEnumId());
        assertEquals(99, result.getSortOrder());
    }

    // ==================== MetadataEnumValue ====================

    @Test
    @DisplayName("toDbEntity MetadataEnumValue: null 输入返回 null")
    public void toDbEntity_enumValue_nullInput_returnsNull() {
        assertNull(DbConverter.toDbEntity((MetadataEnumValue) null));
    }

    @Test
    @DisplayName("toDbEntity MetadataEnumValue: sortOrder 为 null 时默认 0")
    public void toDbEntity_enumValue_nullSortOrder_defaultsToZero() {
        MetadataEnumValue domain = new MetadataEnumValue();
        domain.setEnumId(1L);
        domain.setValueCode("VALUE_01");
        domain.setValueLabel("值1");

        MetadataEnumValueDO result = DbConverter.toDbEntity(domain);

        assertEquals(0, result.getSortOrder(),
                "sortOrder 为 null 时应默认 0，与 DB schema DEFAULT 0 对齐");
    }

    @Test
    @DisplayName("toDbEntity MetadataEnumValue: sortOrder 有值时保留原值")
    public void toDbEntity_enumValue_providedSortOrder_preserved() {
        MetadataEnumValue domain = new MetadataEnumValue();
        domain.setEnumId(1L);
        domain.setValueCode("VALUE_02");
        domain.setValueLabel("值2");
        domain.setSortOrder(10);

        MetadataEnumValueDO result = DbConverter.toDbEntity(domain);

        assertEquals(10, result.getSortOrder(), "明确传入 sortOrder 时应保留原值");
    }
}
