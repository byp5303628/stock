# Controller 编码规范

> 本规则扩展 [api-design.md](./api-design.md)，定义 Controller 层的编码标准和设计约束。

## 1. 注释规范 — 类与方法必须写 JavaDoc

### 1.1 类级别 JavaDoc

每个 Controller 类必须包含 JavaDoc，说明：

- 该 Controller 的职责（负责什么业务域）
- 设计逻辑（为什么这样设计、什么场景使用）

```java
/**
 * 统一金融数据查询写入 Controller。
 *
 * <p>本模块是外部金融数据和内部计算结果的统一读写入口。
 * 所有数据通过 GenericDataDomainService 路由到对应的 fin_{{@code dataType}} 物理表，
 * 业务字段由 metadata 系统定义，无需为每类数据编写独立的 Controller。
 *
 * @author baiyunpeng04
 * @since 2025/07/02
 */
@RestController
@RequestMapping("/api/financial-data")
public class FinancialDataController {
    // ...
}
```

### 1.2 方法级别 JavaDoc

每个方法必须包含 JavaDoc，说明：

- **业务含义**：这个方法做什么
- **设计逻辑**：为什么要这样设计，有什么约束或特殊行为
- **参数说明**：每个入参的用途（使用 `@param`）
- **返回值说明**（使用 `@return`）

```java
/**
 * 查询或分页查询金融数据。
 *
 * <p>当请求中包含 partitionDate + modelCode 时，走精确查询逻辑，
 * 返回单条记录；否则走分页查询逻辑，支持按时间范围和 modelCode 过滤。
 * 分页结果包含 total/page/size/pages 信息。
 *
 * @param request 查询请求体，包含 dataType/code 等参数
 * @return 单条记录或分页结果
 */
@PostMapping("/query")
public ResponseDTO<?> query(@RequestBody @Valid FinancialDataQueryRequest request) {
    // ...
}
```

### 1.3 反例

```java
// ❌ 无注释
@GetMapping
public ResponseDTO<?> query(@RequestParam String dataType, @RequestParam String code) {
    // ...
}

// ❌ 注释只说"查询接口"，没说业务含义和设计逻辑
/**
 * 查询接口
 */
public ResponseDTO<?> query(...) { ... }
```

---

## 2. 参数规范 — 统一使用 Object 入参

### 2.1 规则

**所有 Controller 方法必须使用一个 Request DTO（Object）作为入参**，禁止使用多个 `@RequestParam` 参数。

```java
// ✅ 正确 — 使用 Request DTO
@PostMapping("/query")
public ResponseDTO<?> query(@RequestBody @Valid FinancialDataQueryRequest request) {
    // ...
}

// ❌ 错误 — 多个 @RequestParam 参数
@PostMapping
public ResponseDTO<?> query(
        @RequestParam String dataType,
        @RequestParam String code,
        @RequestParam(required = false) String modelCode) {
    // ...
}
```

### 2.2 理由

| 原因 | 说明 |
|------|------|
| **请求契约清晰** | Request DTO 的字段就是前后端契约，前端一目了然 |
| **参数校验统一** | `@Valid` + Bean Validation 注解，校验逻辑集中管理 |
| **扩展友好** | 新增参数只需在 DTO 加字段，无需改方法签名 |
| **自文档化** | DTO 字段的注释和 `@NotBlank(message)` 本身就是文档 |

### 2.3 Request DTO 设计规范

每个操作定义专用的 Request DTO：

```java
@Getter
@Setter
public class FinancialDataQueryRequest {
    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    /** 市场代码，默认 SH */
    private String market = "SH";

    @NotBlank(message = "code 不能为空")
    private String code;

    /** 模型编码，精确查询时必传 */
    private String modelCode;

    /** 分区日期，精确查询时必传，格式 yyyy-MM-dd */
    private String partitionDate;

    /** 起始日期，分页查询时可选 */
    private String startDate;

    /** 截止日期，分页查询时可选 */
    private String endDate;

    private Integer page = 1;
    private Integer size = 20;
}
```

---

## 3. 通信协议 — 统一使用 POST

### 3.1 规则

**所有 Controller 接口使用 POST 方法**，不再使用 GET。

```java
// ✅ 正确
@PostMapping("/query")
public ResponseDTO<?> query(@RequestBody @Valid FinancialDataQueryRequest request) { ... }

@PostMapping("/upsert")
public ResponseDTO<Void> upsert(@RequestBody @Valid GenericUpsertRequest request) { ... }

// ❌ 错误 — 使用 GET
@GetMapping("/query")
public ResponseDTO<?> query(@RequestParam ...) { ... }
```

### 3.2 原因

- 统一使用 POST + JSON Body，前端同一套调用方式，无需区分 GET/POST
- 复杂查询参数通过 Request DTO 传递，不受 URL 长度限制
- 请求体即契约，前端通过 Request DTO 的字段定义就能知道传什么

### 3.3 端点命名

```
POST /api/{resource}/{action}
```

| 动作 | 端点 | 说明 |
|------|------|------|
| 查询 | `POST /api/financial-data/query` | 统一查询入口 |
| 写入 | `POST /api/financial-data/upsert` | 单条写入 |
| 批量写入 | `POST /api/financial-data/batch-upsert` | 批量写入 |
| 范围替换 | `POST /api/financial-data/replace-range` | 先删后插 |
| 删除 | `POST /api/financial-data/delete` | 删除单条 |

---

## 4. 方法职责规范

Controller 方法只做三件事：

1. **参数校验**（通过 `@Valid` 交给 Bean Validation）
2. **调用 Service**
3. **包装 ResponseDTO**

```java
@PostMapping("/upsert")
public ResponseDTO<Void> upsert(@RequestBody @Valid GenericUpsertRequest request) {
    // 1. 参数校验由 @Valid 自动完成
    // 2. 调用 Service
    GenericDataRecord record = convert(request);
    genericDataService.upsert(request.getDataType(), record);
    // 3. 包装 ResponseDTO
    return ResponseDTO.success();
}
```

**禁止在 Controller 中写业务逻辑、批量计算、数据转换等。** 数据转换逻辑放到 Service 层或专门的 Converter。

---

## 5. 响应用时一致

`updateById` 方法返回更新后的完整对象，而非只返回受影响行数：

```java
@PostMapping("/update")
public ResponseDTO<GenericDataDTO> update(@RequestBody @Valid UpdateRequest request) {
    GenericDataRecord record = genericDataService.update(request.getDataType(), ...);
    return ResponseDTO.success(convert(record));
}
```

---

## 6. 检视清单

- [ ] Controller 类有 JavaDoc（职责 + 设计逻辑）？
- [ ] 每个方法有 JavaDoc（业务含义 + 设计逻辑 + @param + @return）？
- [ ] 方法使用单个 Request DTO 入参？
- [ ] Request DTO 使用 `@Valid` 触发校验？
- [ ] 所有接口使用 POST 方法？
- [ ] Controller 不做业务逻辑？
