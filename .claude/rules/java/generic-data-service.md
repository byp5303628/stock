# 泛化数据服务规范

> 本规则定义 `GenericDataDomainService` 架构下的泛化数据层设计原则和使用规范。

## 1. 架构原则

### 1.1 分层关系

```
metadata 层（Schema 定义）
  metadata_model → 数据类型定义
    └─ metadata_field → 字段定义

存储层（数据实例）
  fin_{dataType} → 每类数据独立成表

泛化读写层
  FinancialDataController → 统一 API /api/financial-data
  GenericDataDomainService → 泛化 CRUD（NamedParameterJdbcTemplate）
  RouteDispatcher → dataType → 物理表路由
```

### 1.2 核心概念映射

| 概念 | 含义 | 示例 |
|------|------|------|
| `dataType` | 路由到物理表的逻辑类型 | `"report"` → `fin_report` |
| `modelCode` | 同一张表内区分不同 metadata schema | `"cash_flow_statement"` |
| `market` | 市场标识 | `SH` / `SZ` / `HK` / `US` |
| `partitionDate` | 计算/查询基准日期 | `2024-12-31` |
| `dataContent` | 实际业务数据 JSON | `{"totalRevenue": 10000}` |

### 1.3 数据流向

```
外部爬虫 → POST /api/financial-data/upsert          → fin_{dataType}
内部计算 → POST /api/financial-data/batch-upsert    → fin_{dataType}
前端查询 → GET  /api/financial-data?dataType=report  → fin_{dataType}
```

---

## 2. 表结构规范

### 2.1 统一建表模板

所有外部金融数据表使用统一结构：

```sql
create table fin_{data_type} (
    id              bigint auto_increment primary key,
    market          varchar(8)   not null comment '市场: SH/SZ/BJ/HK/US',
    code            varchar(32)  not null comment '股票代码',
    model_code      varchar(64)  not null comment '所属 metadata model code',
    partition_date  varchar(16)  not null comment '分区/计算基准日期（ISO 8601）',
    data_content    json         not null comment '业务数据 JSON，由 model_code 对应 metadata schema 定义',
    extra_info      json         comment '扩展信息（来源、版本等）',
    gmt_create      datetime     not null default now(),
    gmt_modified    datetime     not null default now() on update now(),
    unique index uniq_idx_mkt_model_code_date (market, code, model_code, partition_date),
    index idx_market_code (market, code),
    index idx_partition_date (partition_date)
) engine = InnoDB default charset = utf8mb4;
```

### 2.2 设计要点

- **物理隔离**：每类数据独立成表，不影响密集计算的查询性能
- **唯一键 `(market, code, model_code, partition_date)`**：精确定位一条记录，支持 upsert
- **`data_content` JSON**：实际业务数据，key 由 metadata model 的 field 定义
- **`model_code`**：一张物理表可对应多个 metadata model（如 `fin_report` 同时存三张报表）

---

## 3. 路由层规范（RouteDispatcher）

### 3.1 核心逻辑

```
dataType → 本地缓存 → 查 metadata_model 表确认存在 → 返回 fin_{dataType}
```

### 3.2 约束规则

- **表名来源受控**：`resolveTable()` 只返回 `metadata_model` 表中已注册的 `code` 对应的表名，不接受用户直接输入
- **本地缓存**：Caffeine 30s TTL，查不到实时回源
- **Model Code 校验**：写入前必须通过 `resolveModel()` 或显式校验确认 `modelCode` 的 `extInfo.dataType` 等于当前 `dataType`，防止跨模型写入

```java
// RouteDispatcher 路由示例
RouteDispatcher.resolveTable("report")     → "fin_report"
RouteDispatcher.resolveTable("kline")      → "fin_kline"

// Model Code 校验
RouteDispatcher.resolveModel("report", "cash_flow_statement") → MetadataModel ✅
RouteDispatcher.resolveModel("report", "kline_daily")         → throws ❌
```

---

## 4. 写入规范

### 4.1 使用 upsert 语义

所有写入操作使用 `ON DUPLICATE KEY UPDATE`，不允许先查再判断插入/更新：

```sql
insert into fin_{dataType} (market, code, model_code, partition_date, data_content, extra_info)
values (:market, :code, :modelCode, :partitionDate, :dataContent, :extraInfo)
on duplicate key update
    data_content = values(data_content),
    extra_info   = values(extra_info);
```

### 4.2 批量操作

批量 upsert 使用 `NamedParameterJdbcTemplate.batchUpdate()` 一次提交：

```java
MapSqlParameterSource[] batchParams = records.stream()
        .map(this::toParams)
        .toArray(MapSqlParameterSource[]::new);
jdbcTemplate.batchUpdate(sql, batchParams);
```

### 4.3 范围替换必须加事务

`replaceRange`（先删后插）必须在 `@Transactional` 中执行，防止 delete 成功后 insert 失败导致数据丢失：

```java
@Transactional(rollbackFor = Exception.class)
public void replaceRange(String dataType, String market, String code, String modelCode,
                         String startDate, String endDate, List<GenericDataRecord> records) {
    deleteByRange(dataType, market, code, modelCode, startDate, endDate);
    if (records != null && !records.isEmpty()) {
        batchUpsert(dataType, records);
    }
}
```

---

## 5. 查询规范

### 5.1 参数化查询

所有 SQL 使用 `NamedParameterJdbcTemplate` 的命名参数占位符，不使用字符串拼接：

```java
// ✅ 正确 — 命名参数
String sql = "select * from " + table + " where market = :market and code = :code";
MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("market", market)
        .addValue("code", code);

// ❌ 错误 — 字符串拼接
String sql = "select * from " + table + " where market = '" + market + "'";
```

### 5.2 动态 WHERE 构建

多条件查询使用 StringBuilder 拼接 WHERE 子句，配合 `MapSqlParameterSource`：

```java
StringBuilder where = new StringBuilder(" where 1=1");
MapSqlParameterSource params = new MapSqlParameterSource();

if (query.getMarket() != null) {
    where.append(" and market = :market");
    params.addValue("market", query.getMarket());
}
// ...

String sql = String.format("select * from %s%s order by partition_date desc limit :offset, :limit",
        table, where);
```

### 5.3 分页

所有分页查询必须同时提供 `count()` 和带 limit 的数据查询，统一使用 `PageResult<T>` 返回。

---

## 6. 新增数据类型的流程

新增一种外部金融数据只需要两步（不需要写任何 Java 代码）：

1. **建表**：按 2.1 模板创建 `fin_{dataType}` 表
2. **注册 metadata model**：在 metadata 系统中创建对应的 model 和 fields，`extInfo` 中写入 `dataType: "{类型编码}"`

```json
// metadata_model.extInfo 示例
{
  "dataType": "report",
  "description": "现金流量表"
}
```

---

## 7. 安全性约束

| 约束 | 说明 |
|------|------|
| 动态表名来源 | 只来自 metadata_model 已注册的 `code`，不接受用户输入 |
| 参数化查询 | 所有用户输入通过 `NamedParameterJdbcTemplate` 占位符传入 |
| Model Code 校验 | 写入/删除前校验 `modelCode` 属于当前 `dataType` |
| 事务保证 | `replaceRange` 必须在 `@Transactional` 中执行 |

---

## 8. 检视清单

- [ ] 新表使用统一 `fin_{dataType}` 命名规范？
- [ ] 写入操作使用 upsert 语义？
- [ ] 批量 upsert 使用 `batchUpdate()`？
- [ ] `replaceRange` 加了 `@Transactional`？
- [ ] 所有 SQL 使用命名参数占位符？
- [ ] 分页查询使用 `PageResult<T>`？
- [ ] RouteDispatcher 的缓存配置正确？
- [ ] metadata model 的 `extInfo` 包含 `dataType`？
- [ ] 新增数据类型不需要写 Java 代码？
- [ ] 表名不直接来自用户输入？
