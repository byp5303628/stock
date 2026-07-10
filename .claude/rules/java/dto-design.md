# DTO 设计规范

> 本规则扩展 [api-design.md](./api-design.md)，定义 DTO 的设计标准和 Bean Validation 规范。

## 1. DTO 命名规范

### 1.1 后缀规则

| 类型 | 后缀 | 示例 |
|------|------|------|
| 请求 DTO（POST/PUT Body） | `Request` | `GenericUpsertRequest`、`StrategyCreateRequest` |
| 响应 DTO（返回给前端） | `DTO` | `GenericDataDTO`、`StrategyDTO` |
| 查询参数（GET 参数） | `Query` 或无后缀 | `GenericDataQuery` |

**不允许的命名：**
- ❌ `XXXReq`、`XXXResp`、`XXXVO`、`XXXBO`
- ❌ 复用 Entity/DO 作为请求体或响应体
- ❌ 同一个类既做请求又做响应

### 1.2 每个操作专用 Request

每个 API 操作定义专用的 Request DTO，不复用其他接口的 Request：

```java
// ✅ 正确 — 每个操作独立定义
public class StrategyCreateRequest { ... }
public class StrategyUpdateRequest { ... }

// ❌ 错误 — 用一个类覆盖多个操作
public class StrategyRequest { ... }
```

### 1.3 响应 DTO 扁平化

响应 DTO 控制在 2 层以内，不深层嵌套：

```json
// ✅ 扁平
{ "market": "SH", "code": "000001", "dataContent": {...} }

// ❌ 3 层嵌套
{ "result": { "stock": { "market": "SH", "code": "000001" } } }
```

---

## 2. Bean Validation 规范

### 2.1 启用方式

Controller 方法参数加 `@Valid`：

```java
@PostMapping("/upsert")
public ResponseDTO<Void> upsert(@RequestBody @Valid GenericUpsertRequest request) {
    // ...
}
```

### 2.2 常用校验注解

| 注解 | 适用类型 | 说明 |
|------|---------|------|
| `@NotBlank(message = "...")` | String | 非 null 且去掉空格后非空 |
| `@NotEmpty(message = "...")` | String/Collection/Array | 非 null 且长度 > 0 |
| `@NotNull(message = "...")` | 所有类型 | 非 null |
| `@Size(min, max)` | String/Collection | 长度限制 |

### 2.3 message 属性强制

**所有校验注解必须带 `message` 属性，使用中文描述错误原因：**

```java
// ✅ 正确
@NotBlank(message = "股票代码不能为空")
private String code;

// ❌ 错误 — 无 message 属性，Spring 返回默认英文消息
@NotBlank
private String code;

// ❌ 错误 — 英文消息
@NotBlank(message = "code cannot be empty")
private String code;
```

**message 格式：** `"{字段中文名}不能为空"` 或 `"{字段中文名}格式不合法"`，禁止使用笼统的 `"参数错误"`。

### 2.4 嵌套校验

嵌套对象使用 `@Valid` 触发递归校验：

```java
public class GenericBatchUpsertRequest {
    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    @NotEmpty(message = "records 不能为空")
    @Valid
    private List<RecordItem> records;

    public static class RecordItem {
        @NotBlank(message = "market 不能为空")
        private String market;

        @NotBlank(message = "code 不能为空")
        private String code;
        // ...
    }
}
```

### 2.5 全局异常处理

校验失败由 `GlobalExceptionHandler` 统一处理，抛出 `MethodArgumentNotValidException`：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseDTO<Void> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return ResponseDTO.error(40001, msg);
}
```

响应示例：
```json
{
  "code": 40001,
  "msg": "dataType: dataType 不能为空; market: market 不能为空",
  "data": null
}
```

### 2.6 JSON 解析失败

`HttpMessageNotReadableException`（JSON 格式错误、类型不匹配）由 `GlobalExceptionHandler` 统一处理：

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseDTO<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
    return ResponseDTO.error(40001, "请求体格式错误，请检查 JSON");
}
```

---

## 3. 响应规范

### 3.1 枚举输出

所有枚举字段输出**字符串值**，不输出数字序号：

```json
// ✅ 正确
{ "reportType": "CASH_FLOW" }

// ❌ 错误
{ "reportType": 0 }
```

### 3.2 日期格式

- 日期：`yyyy-MM-dd` → `"2024-12-31"`
- 日期时间：`yyyy-MM-dd HH:mm:ss` → `"2024-12-31 14:30:00"`
- 统一使用 `Asia/Shanghai` 时区

### 3.3 字段命名

- JSON key 使用 camelCase，与 Java 字段名一致
- 字段名自解释，不用模糊缩写
- ❌ `cnt`、`amt`、`info`、`ext`
- ✅ `totalCount`、`totalAmount`、`extraInfo`

---

## 4. 检视清单

- [ ] Request DTO 是否以 `Request` 结尾？
- [ ] Response DTO 是否以 `DTO` 结尾？
- [ ] 是否每个操作有自己的 Request DTO？
- [ ] 所有 `@NotBlank`/`@NotEmpty` 带中文 `message`？
- [ ] 嵌套集合使用 `@Valid`？
- [ ] 枚举输出字符串值？
- [ ] 日期使用 ISO 8601 格式？
- [ ] 字段名自解释，无模糊缩写？
