# 设计原则

> 顶层设计哲学。解释项目的核心决策逻辑，方便后续开发时做判断。
> 每个原则都关联具体的 rule 文件，详见"参考规则"。

---

## 原则一：消灭重复代码 — 泛化优先

**同一类模式的操作，只写一次。**

### 问题

金融数据有财报、K 线、衍生指标等种类。每新增一类，过去需要重复写：

```
Mapper + XML + DO + DomainModel + DomainService + Controller + DTO + Converter = 10+ 个文件
```

### 解决方案

**泛化数据层** — 同一类读写模式用一套泛化 Service 接管，新增数据类型只需建表 + 注册 metadata model，无需写 Java 代码。

```java
// 新增财报（过去）：FinancialReportMapper + XML + DO + Service + Controller...
// 新增财报（现在）：建 fin_report 表 + metadata model 定义字段 → 完成
```

### 适用场景

- 外部金融数据的写入和查询 → 走 `GenericDataDomainService`
- 内部计算结果的回填 → 走 `GenericDataDomainService.batchUpsert()`

**参考规则：** `rules/java/generic-data-service.md`

---

## 原则二：每类数据独立成表 — 不单表混存

**物理隔离保障计算性能。**

### 问题

如果所有数据塞一张表（`fin_data(dataType, code, partitionDate, content)`），分析查询和批量计算会互相干扰。

### 解决方案

每类数据一张表，结构相同但物理隔离：

```
fin_report      → 财报数据
fin_kline       → K 线数据
fin_indicator   → 衍生指标
```

唯一键统一为 `(market, code, model_code, partition_date)`。

### 适用场景

- 任何从外部获取或内部计算产生的金融时序数据

**参考规则：** `rules/java/generic-data-service.md`

---

## 原则三：契约驱动 — POST + Request DTO

**所有接口使用 POST + JSON Body，请求即契约。**

### 问题

过去 Controller 方法用多个 `@RequestParam` 参数，参数多时通常有 7-9 个，没法用 `@Valid` 统一校验，前端也不知道该传什么。

### 解决方案

- 所有接口使用 `POST`（不用 GET）
- 每个接口定义一个专用的 Request DTO（`@RequestBody @Valid`）
- DTO 放在 `dto/request/` 包，以 `Request` 结尾
- 嵌套 DTO 也必须带 `DTO` 后缀

```java
@PostMapping("/query")
public ResponseDTO<?> query(@RequestBody @Valid FinancialDataQueryRequest request) { ... }
```

**参考规则：** `rules/java/controller-coding.md`、`rules/java/dto-design.md`、`rules/java/api-design.md`

---

## 原则四：上游感知结果 — Result 包裹

**Domain Service 不返回裸类型。**

### 问题

`get()` 返回 null 代表"不存在"还是"出错"？调用方无法区分。

### 解决方案

查询方法返回 `Result<T>`，调用方先检查 `isSuccess()`，再读取 `getData()`：

```java
Result<GenericDataRecord> result = service.get(key);
if (!result.isSuccess()) {
    // 处理错误
}
if (result.getData() == null) {
    // 不存在（正常语义）
}
```

分页查询返回 `PageResult<T>`。

**参考规则：** `rules/java/domain-service-result.md`

---

## 原则五：元数据驱动 — 字段不写死在代码里

**数据字段的 schema 由 metadata 系统定义，不是写死在表结构或代码中。**

### 问题

过去每类数据的字段结构不同，每新增一个字段就要改表结构、改代码、改接口。

### 解决方案

表结构统一为 `data_content JSON`，字段 key-value 由 `metadata_model` + `metadata_field` 定义：

```sql
-- 物理表只有 8 个标准字段
data_content JSON  → { "totalRevenue": 1000, "netProfit": 200 }
                         ↑ 字段名和含义由 metadata 系统定义
```

新增字段 → 在 metadata 系统中加一条 field 定义，无需改表结构。

**参考规则：** `rules/java/generic-data-service.md`

---

## 原则六：枚举管理初始化 — 不散落 @PostConstruct

**启动时执行的初始化任务统一注册，不分散在多个 Bean 中。**

### 问题

过去 `FinancialMetadataInitializer` 是一个 `@PostConstruct` Component，业务逻辑和字段定义耦合在一起。未来每加一个爬虫就得再写一个 `@PostConstruct`。

### 解决方案

`InitializerDefinition` 枚举 + `MetadataInitializer` 接口：

```java
// 枚举只注册（轻量）
CASH_FLOW_STATEMENT(METADATA, CashFlowStatementInitializer.class),
BALANCE_SHEET(METADATA, BalanceSheetInitializer.class),

// 字段定义在独立的实现类中
public class CashFlowStatementInitializer implements MetadataInitializer {
    public List<FieldDef> getFields() { return List.of(...); }
}
```

**参考规则：** 详见 `InitializerDefinition.java` 和 `MetadataInitializer.java`

---

## 原则七：统一依赖注入 — @Resource

**所有 Bean 注入使用 `@Resource` 字段注入，不用构造器注入和 `@Autowired`。**

```java
// ✅ 正确
@Resource
private MetadataDomainService metadataDomainService;

// ❌ 错误 — 构造器注入
private final MetadataDomainService metadataDomainService;
public Xxx(MetadataDomainService metadataDomainService) { ... }
```

**参考规则：** `rules/java/patterns.md`

---

## 原则八：统一日志 — @Slf4j

**使用 Lombok `@Slf4j` 注解，不使用手动 Logger 声明。**

```java
// ✅ 正确
@Slf4j
@Component
public class Xxx { ... }

// ❌ 错误
private static final Logger log = LoggerFactory.getLogger(Xxx.class);
```

**参考规则：** `rules/java/logging.md`

---

## 原则九：统一异常处理 — GlobalExceptionHandler

**Controller 不自己 try-catch，所有异常由 GlobalExceptionHandler 收敛。**

当前已覆盖：

| 异常类型 | 返回 code |
|---------|-----------|
| `MethodArgumentNotValidException` | 202（参数校验失败）|
| `MissingServletRequestParameterException` | 202（缺少必填参数）|
| `HttpMessageNotReadableException` | 202（JSON 格式错误）|
| `BusinessException` | 自定义业务码 |
| `Exception` | 208（系统内部错误）|

**参考规则：** 详见 `GlobalExceptionHandler.java`

---

## 总结：新增一个数据类型的标准流程

```
1. 建表        → CREATE TABLE fin_{dataType}（统一模板）
2. 注册 metadata → 实现 MetadataInitializer，枚举加一行
3. 写数据      → POST /api/financial-data/upsert（已有 Controller，无需改代码）
4. 查数据      → POST /api/financial-data/query（已有 Controller，无需改代码）
```

**新增元数据模型流程：**

```
1. 写 MetadataInitializer 实现类（定义 code/name/fields）
2. InitializerDefinition 枚举加一行
3. 启动 → InitializerRunner 自动执行
```
