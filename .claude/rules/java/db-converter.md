---
paths:
  - "**/converter/DbConverter.java"
  - "**/DbConverter.java"
  - "**/DbConverter*.java"
globs:
  - "**/*DbConverter*.java"
  - "**/*Converter*.java"
description: DB 字段默认值同步规则 — 防止 NOT NULL DEFAULT 列因 Java 侧传 null 导致 SQL 报错
---

# DB 字段默认值同步规则

> 防止 DB 列有 `NOT NULL DEFAULT xxx` 但 Java 转换层显式传 `null`，导致 MySQL 报 `Column 'xxx' cannot be null`。

## 问题模式

### 典型报错

```
SQLIntegrityConstraintViolationException: Column 'status' cannot be null
SQLIntegrityConstraintViolationException: Column 'sort_order' cannot be null
```

### 根因链路

```
前端 DTO（不传 status 字段）
  → DtoConverter.toDomain()（不设置 status，保持 null）
    → DbConverter.toDbEntity()（直接透传 null 到 DO）
      → MyBatis INSERT（显式传 null）
        → MySQL: Column 'status' cannot be null ❌
```

**为什么 DB 有 `DEFAULT 'DRAFT'` 但不起作用？**
MySQL 只有在 INSERT 语句**不包含该列**时才使用 DEFAULT。当 MyBatis 显式传 `null` 时，`null` 优先级高于 DEFAULT。

### 反例（错误代码）

```java
// ❌ 错误：NOT NULL 字段直接透传，可能为 null
public static MetadataModelDO toDbEntity(MetadataModel domain) {
    MetadataModelDO dbEntity = new MetadataModelDO();
    dbEntity.setStatus(domain.getStatus());   // domain.getStatus() 可能为 null！
    dbEntity.setSortOrder(domain.getSortOrder());
    dbEntity.setBusinessMeaning(domain.getBusinessMeaning());
    return dbEntity;
}
```

### 正例（正确代码）

```java
// ✅ 正确：每个 NOT NULL 字段都与 DB DEFAULT 对齐的 null-safe 默认值
public static MetadataModelDO toDbEntity(MetadataModel domain) {
    MetadataModelDO dbEntity = new MetadataModelDO();
    dbEntity.setStatus(domain.getStatus() == null ? "DRAFT" : domain.getStatus());
    dbEntity.setSortOrder(domain.getSortOrder() == null ? 0 : domain.getSortOrder());
    dbEntity.setBusinessMeaning(domain.getBusinessMeaning() == null ? "" : domain.getBusinessMeaning());
    return dbEntity;
}
```

## 强制规则

### R1: DbConverter 中每个 DB NOT NULL 列必须有 null-safe 默认值

新增或修改 `DbConverter.toDbEntity()` 时，对**每一个** DB 中有 `NOT NULL` 约束的列，必须在 setter 中提供 null-safe 默认值：

```java
dbEntity.setXxx(domain.getXxx() == null ? <DB_DEFAULT_VALUE> : domain.getXxx());
```

**默认值必须与 `basic_init.sql` 中 `DEFAULT` 子句一致。**

### R2: 新增实体时必须全量检查 DB schema

新增 `*DO.java` 类时，必须：
1. 打开 `basic_init.sql`，找到对应 `CREATE TABLE`
2. 列出所有 `NOT NULL` 的列
3. 检查每个列是否有 `DEFAULT xxx`
4. 在 `DbConverter.toDbEntity()` 中逐一添加 null-safe 默认值

### R3: 修改表结构时必须同步更新 DbConverter

执行 `ALTER TABLE` 新增列或修改 DEFAULT 后，必须同步更新 `DbConverter` 中的对应逻辑。

## 对照表模板

每新增一个实体，在 DbConverter 的 Javadoc 中标注默认值映射：

```java
/**
 * 将 Xxx 领域对象转换为 XxxDO。
 *
 * <p>NOT NULL 默认值（与 basic_init.sql 对齐）：
 * <ul>
 *   <li>status → "DRAFT"</li>
 *   <li>sortOrder → 0</li>
 *   <li>businessMeaning → ""</li>
 * </ul>
 */
public static XxxDO toDbEntity(Xxx domain) { ... }
```

## DB Schema 速查

`basic_init.sql` 中当前所有 `NOT NULL` 且 `DEFAULT` 非 `NULL` 的列：

| 表 | 列 | DEFAULT | DbConverter 默认值 |
|----|-----|---------|-------------------|
| `metadata_model` | `status` | `'DRAFT'` | `"DRAFT"` |
| `metadata_model` | `description` | `''` | —（前端 `@NotBlank` 保证非空） |
| `metadata_field` | `business_meaning` | `''` | `""` |
| `metadata_field` | `sort_order` | `0` | `0` |
| `metadata_field` | `required` | `0` | `0`（三目运算） |
| `metadata_enum` | `status` | `'ENABLED'` | `"ENABLED"` |
| `metadata_enum` | `description` | `''` | —（可空字段） |
| `metadata_enum_value` | `sort_order` | `0` | `0` |
| `metadata_enum_value` | `ext_info` | — | `null`（JSON 列允许 null） |

## 检查清单

在提交包含 `DbConverter` / `*DO` / `basic_init.sql` 修改的代码前：

- [ ] 每个新增的 DB NOT NULL 列在 DbConverter 中有对应的 null-safe 默认值
- [ ] 默认值与 `basic_init.sql` 中的 `DEFAULT` 一致
- [ ] 已通过 `curl` 或集成测试验证 INSERT 不会触发 `Column 'xxx' cannot be null`
- [ ] DbConverter Javadoc 中已标注默认值映射
