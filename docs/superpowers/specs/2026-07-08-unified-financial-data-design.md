# 统一金融数据存储设计

> 日期: 2026-07-08
> 状态: 设计稿
> 作者: Ethan Park

## 1. 背景与目标

### 1.1 现状

项目目前存在多份外部金融数据（财报、K 线、衍生指标等），每类数据独立建表、独立写 Mapper XML、独立写 DomainService 和 Controller。以 `financial_report` 为例：

- 1 张表 + 1 个 DO + 1 个 Mapper 接口 + 1 个 Mapper XML
- 1 个 Domain 模型 + 1 个 DomainService 接口 + 1 个 impl
- 1 个 Controller + 多个 DTO + 转换器

每新增一类外部数据，重复全部 10+ 个文件。

### 1.2 目标

- **存储层统一**：所有外部金融数据共用同一套表结构，物理上按数据类型分表
- **读写层泛化**：Mapper / DomainService / Controller 层提供一套泛化能力，不因数据类型不同而重复
- **Schema 元数据化**：每类数据的字段定义由 metadata 系统管理，新增数据类型不需写 Java 代码
- **保留计算效率**：每类数据独立成表，不影响密集计算的查询性能

### 1.3 范围

**纳入统一体系**：所有从外部接口获取或内部计算产生的金融时序数据。
**不纳入**：metadata 系统表、策略回测表（`trade_policy_regression` 等）、任务调度表、前端系统配置表等内部业务实体。

---

## 2. 核心概念

### 2.1 分层关系

```
metadata 层（Schema 定义）
  metadata_model          → 数据类型定义（code = "report" 等）
    └─ metadata_field     → 字段定义（名称、类型、校验、枚举绑定）
      └─ metadata_enum    → 枚举定义

存储层（数据实例）
  fin_report              → 财报数据（对应多个 metadata model）
  fin_kline               → 日K线数据（未来）
  fin_indicator           → 衍生指标数据（未来）

泛化读写层（统一服务）
  GenericDataController   → /api/financial-data
  GenericDataService      → 泛化 CRUD
  DynamicTableMapper      → NamedParameterJdbcTemplate
```

### 2.2 关键概念映射

| 概念 | 含义 | 示例 |
|------|------|------|
| `dataType` | 路由到物理表的逻辑类型 | `"report"` → `fin_report` |
| `modelCode` | 同一张表内区分不同 metadata schema | `"cash_flow_statement"` / `"balance_sheet"` |
| `market` | 市场标识 | `SH` / `SZ` / `BJ` / `HK` / `US` |
| `partitionDate` | 计算/查询基准日期 | `2024-12-31` |
| `dataContent` | 实际业务数据 JSON | `{"totalRevenue": 10000, "netProfit": 2000}` |

---

## 3. 数据库设计

### 3.1 统一表结构

```sql
create table fin_{data_type} (
    id              bigint auto_increment primary key,
    market          varchar(8)   not null comment '市场: SH/SZ/BJ/HK/US',
    code            varchar(32)  not null comment '股票代码',
    model_code      varchar(64)  not null comment '所属 metadata model code',
    partition_date  varchar(16)  not null comment '分区/计算基准日期（ISO 8601）',
    data_content    json         not null comment '业务数据 JSON，由 model_code 对应的 metadata schema 定义',
    extra_info      json         comment '扩展信息（来源、版本号等）',
    gmt_create      datetime     not null default now(),
    gmt_modified    datetime     not null default now() on update now(),
    unique index uniq_idx_mkt_model_code_date (market, code, model_code, partition_date),
    index idx_market_code (market, code),
    index idx_partition_date (partition_date),
    index idx_gmt_modified (gmt_modified)
) engine = InnoDB default charset = utf8mb4 comment 'fin_{data_type}: {data_type} 金融数据表';
```

### 3.2 设计要点

- **`data_content` JSON** — 存储实际业务数据。key 由 `model_code` 对应的 metadata 字段定义，不写死在表结构中
- **`model_code`** — 一张物理表可对应多个 metadata model。例：`fin_report` 表同时存 `cash_flow_statement`、`balance_sheet`、`income_statement` 三套模型的数据
- **`extra_info` JSON** — 扩展信息（数据来源、爬取版本、计算版本等），不参与唯一键
- **唯一键 `(market, code, model_code, partition_date)`** — 精确定位一条记录，支持 `ON DUPLICATE KEY UPDATE`

### 3.3 物理表清单

| 表名 | 对应 dataType | 说明 |
|------|-------------|------|
| `fin_report` | `report` | 财务报告数据（迁移自现有 `financial_report`） |
| `fin_kline_daily` | `kline_daily` | 日K线数据（未来） |
| `fin_indicator` | `indicator` | 衍生计算指标数据（未来） |

### 3.4 迁移策略

现有 `financial_report` 表原地重命名，保留全部历史数据：

```sql
alter table financial_report rename to fin_report;
```

对应地，`FinancialMetadataInitializer` 中三个 model 的 `extInfo` 补充 `dataType: "report"` 标识，用于路由层解析。

---

## 4. 路由层设计（RouteDispatcher）

### 4.1 核心逻辑

```
dataType → 本地缓存 → 查 metadata_model 表确认存在 → resolve table name
```

```
fin_{dataType}
```

### 4.2 缓存策略

- 本地缓存：Caffeine（30 秒 TTL，最大 100 条）
- Key：`dataType`
- Value：`MetadataModel` 对象（含 code、modelType、extInfo 等）
- 回源：缓存未命中 → 查 `MetadataDomainService.getModelByCode(dataType)` → 写入缓存
- 保证：只有 metadata_model 表中已注册的 dataType 才能通过路由，防止表名注入

### 4.3 伪代码

```java
@Component
public class RouteDispatcher {

    @Resource
    private MetadataDomainService metadataDomainService;

    private final Cache<String, MetadataModel> routeCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    /**
     * 解析 dataType 为物理表名。
     *
     * @param dataType 数据类型编码
     * @return 物理表名，如 "fin_report"
     * @throws IllegalArgumentException dataType 未注册时抛出
     */
    public String resolveTable(String dataType) {
        MetadataModel model = routeCache.get(dataType, key -> {
            MetadataModel m = metadataDomainService.getModelByCode(key);
            if (m == null) {
                throw new IllegalArgumentException("未知 dataType: " + key);
            }
            return m;
        });
        return "fin_" + dataType;
    }

    /**
     * 校验 dataType 是否存在。
     */
    public boolean exists(String dataType) {
        try {
            resolveTable(dataType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

## 5. Service 层设计（GenericDataService）

### 5.1 能力清单

#### 读取

```java
// 精确查询 — (market, code, modelCode, partitionDate) 唯一键定位
GenericDataRecord get(String dataType, String market, String code, String modelCode, String partitionDate);

// 分页查询 — 支持按 market/code/modelCode、日期范围过滤
PageResult<GenericDataRecord> queryPage(String dataType, GenericDataQuery query);

// 时间范围批量读取 — 批量计算取数据源
List<GenericDataRecord> queryRange(String dataType, String market, String code, String modelCode,
                                   String startDate, String endDate);

// 取最新 N 条
List<GenericDataRecord> queryLatest(String dataType, String market, String code, int limit);

// 按 modelCode 取最新各类型一条（原 selectLatestByCode 语义）
List<GenericDataRecord> queryLatestByModel(String dataType, String market, String code);
```

#### 写入

```java
// 单条 upsert
void upsert(String dataType, GenericDataRecord record);

// 批量 upsert — 内部计算批量写入
void batchUpsert(String dataType, List<GenericDataRecord> records);

// 范围替换 — 先删后插，用于重算指标
void replaceRange(String dataType, String market, String code, String modelCode,
                  String startDate, String endDate, List<GenericDataRecord> records);
```

#### 删除

```java
// 单条删除
void delete(String dataType, String market, String code, String modelCode, String partitionDate);

// 范围删除
void deleteByRange(String dataType, String market, String code, String modelCode,
                   String startDate, String endDate);
```

### 5.2 DB 写入语义

所有 upsert 操作使用 `ON DUPLICATE KEY UPDATE`：

```sql
insert into fin_{dataType} (market, code, model_code, partition_date, data_content, extra_info)
values (:market, :code, :modelCode, :partitionDate, :dataContent, :extraInfo)
on duplicate key update
    data_content = values(data_content),
    extra_info   = values(extra_info);
```

`replaceRange` 先执行 `deleteByRange` 再执行 `batchUpsert`，整个操作在一个 `@Transactional` 中完成。

### 5.3 数据模型

```java
/**
 * 泛化数据记录 — 代表 fin_{dataType} 表中的一行。
 */
@Getter
@Setter
public class GenericDataRecord {
    private Long id;
    private String market;
    private String code;
    private String modelCode;
    private String partitionDate;
    private Map<String, Object> dataContent;
    private Map<String, Object> extraInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
```

### 5.4 实现策略

使用 Spring `NamedParameterJdbcTemplate` 替代 MyBatis，由路由层先解析表名，再拼接 SQL：

```java
@Service
public class GenericDataServiceImpl implements GenericDataService {

    @Resource
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Resource
    private RouteDispatcher routeDispatcher;

    @Override
    public void upsert(String dataType, GenericDataRecord record) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "insert into %s (market, code, model_code, partition_date, data_content, extra_info) " +
            "values (:market, :code, :modelCode, :partitionDate, :dataContent, :extraInfo) " +
            "on duplicate key update data_content = values(data_content), extra_info = values(extra_info)",
            table
        );

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("market", record.getMarket())
            .addValue("code", record.getCode())
            .addValue("modelCode", record.getModelCode())
            .addValue("partitionDate", record.getPartitionDate())
            .addValue("dataContent", JsonUtil.toJsonString(record.getDataContent()))
            .addValue("extraInfo", JsonUtil.toJsonString(record.getExtraInfo()));

        jdbcTemplate.update(sql, params);
    }

    // ... 其他方法同上模式
}
```

**安全性**：`resolveTable()` 返回的表名来自 `metadata_model.code` 的校验结果，不是用户输入。SQL 的其余部分全是参数化占位符（`:xxx`），无任何拼接风险。

---

## 6. Controller 层设计（FinancialDataController）

### 6.1 路由规范

| 方法 | 端点 | 说明 |
|------|------|------|
| `GET` | `/api/financial-data` | 精确查询或分页查询 |
| `POST` | `/api/financial-data/upsert` | 单条 upsert |
| `POST` | `/api/financial-data/batch-upsert` | 批量 upsert |
| `POST` | `/api/financial-data/replace-range` | 范围替换（先删后插） |
| `POST` | `/api/financial-data/delete` | 删除 |

### 6.2 查询参数

精确查询示例：
```
GET /api/financial-data?dataType=report&market=SH&code=000001&modelCode=cash_flow_statement&partitionDate=2024-12-31
```

分页查询示例：
```
GET /api/financial-data?dataType=report&market=SH&code=000001&startDate=2024-01-01&endDate=2024-12-31&page=1&size=20
```

### 6.3 写入 Body

```json
{
  "dataType": "report",
  "market": "SH",
  "code": "000001",
  "modelCode": "cash_flow_statement",
  "partitionDate": "2024-12-31",
  "dataContent": {
    "totalRevenue": 10000000000,
    "netProfit": 2000000000
  },
  "extraInfo": {
    "source": "sina",
    "version": "1.0"
  }
}
```

### 6.4 后续扩展

- `queryLatestByModel` 可在 Controller 层实现为：`GET /api/financial-data/latest?dataType=report&code=000001&market=SH` — 查某只股票的各类最新数据

---

## 7. 与现有系统的关系

### 7.1 保留不动

| 模块 | 说明 |
|------|------|
| `metadata_*` 表 + 全部 metadata 代码 | Model/Field/Enum CRUD、Schema 校验、版本管理、JSONSchema 生成，全部保留 |
| `stock_*` 策略回测相关 | 策略配置、回归结果、任务调度等内部业务实体各自保留独立 Mapper |
| `MetadataController` / `MetadataIntegrationController` | 作为 Schema 定义层 API，保留不动 |

### 7.2 替换掉

| 现有类 | 替换为 |
|--------|--------|
| `FinancialReportMapper.java` + XML | 删除，由 `GenericDataService` 接管 |
| `FinancialReportDomainService.java` + impl | 删除，由 `GenericDataService` 接管 |
| `FinancialReportController.java` | 删除，由 `FinancialDataController` 接管 |
| `FinancialReportDO.java` | 删除，由 `GenericDataRecord` 替代 |
| `FinancialReport.java` (domain model) | 删除 |
| `FinancialReportDTO` / `FinancialReportSaveRequest` / `FinancialReportQueryRequest` | 删除 |
| `DtoConverter.java` 中 FinancialReport 相关 | 删除 |
| `DomainConverter.java` 中 FinancialReport 相关 | 删除 |
| `DbConverter.java` 中 FinancialReport 相关 | 删除 |
| `FinancialMetadataInitializer.java` | **保留**，但 `extInfo` 补充 `dataType` 信息 |

### 7.3 metadata model 的适配

现有 `FinancialMetadataInitializer` 创建的三个 model，需要在 `extInfo` 中补充 `dataType` 字段，供路由层解析：

```json
// cash_flow_statement 模型的 extInfo
{
  "dataType": "report",
  "description": "现金流量表"
}
```

### 7.4 FinancialMetadataInitializer 后续演进

长期看，`FinancialMetadataInitializer` 应该从一个硬编码的初始化器演化为**建表 + metadata model 注册的一体化工具**——新增一种数据源时，自动完成建表 + 创建 metadata model 两步。

但目前将其简化：手动建表 + 初始化器中补充 `extInfo.dataType` 即可。

---

## 8. 安全性考虑

### 8.1 动态表名

动态表名拼接来自 `resolveTable()` 方法，该方法只允许 `metadata_model` 表中已注册的 `code` 值通过，不存在 SQL 注入风险。所有用户输入的值都通过 `NamedParameterJdbcTemplate` 的参数化占位符传入。

### 8.2 数据隔离

每类数据独立成表，不会出现 `dataType=report` 的查询误入 `kline_daily` 表的情况。

### 8.3 Model Code 校验

`modelCode` 虽然是用户输入，但在 Service 层会校验其合法性：

1. 查 metadata_model 表确认 modelCode 存在
2. 确认该 model 的 `extInfo.dataType` 等于当前请求的 `dataType`
3. 防止跨模型写入（如将 `kline_daily` 的 modelCode 写入 `fin_report` 表）

---

## 9. 未来扩展

- **计算回填场景**：内部计算产生的指标数据（如动量指标、波动率等）存入对应 `fin_indicator` 表，走同一套写入 API
- **多市场支持**：`market` 字段预留 `HK`/`US`，后续只需 metadata 层定义对应模型即可支持
- **大表分片**：当某类数据量极大（如 tick 级数据），可在命名约定基础上扩展为 `fin_{dataType}_{shard}` 或按月分区
- **监控与统计**：可基于 `idx_gmt_modified` 索引监控每张表的新增速率，用于数据源健康度检测

---

## 10. 开放问题

- **计算代码**：现有基于 `FinancialReport` domain model 的计算逻辑（如指标派生），需要改为基于 `GenericDataRecord` 的泛化计算，或仍然保留强类型中间层？这取决于计算复杂度，建议在实施阶段分析。
