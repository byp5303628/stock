---
paths:
  - "**/*.java"
---
# Java Web API 设计规范：Curl 友好 & AI 友好

> 本规则扩展 [patterns.md](./patterns.md) 和 [coding-style.md](./coding-style.md)，定义 Web API 的设计标准。

## 设计目标

每个接口应同时满足：

| 目标 | 含义 |
|------|------|
| **面向 Curl 友好** | 看一眼 URL 和参数就知道怎么构造 curl 命令，不需要查文档 |
| **面向 CLI 友好** | 能直接转成标准 CLI 工具调用（curl/httpie/wget），无交互、无状态 |
| **面向 AI 友好** | AI 从 URL 和参数名就能推断接口行为，响应字段自解释 |

---

## 1. URL 设计

### 1.1 基础格式

```
/api/{resource-plural}[/{action}]
```

所有接口保持两层结构，不深层嵌套。

### 1.2 命名规则

| 规则 | 正确 ✅ | 错误 ❌ |
|------|---------|---------|
| 小写 + 连字符 | `/api/stock-strategies` | `/api/stockStrategies`、`/api/stock_strategies` |
| 资源用复数 | `/api/strategy-details` | `/api/strategy-detail` |
| 2 层以内 | `/api/stock-strategies/detail` | `/api/stock/strategy/detail/code` |
| 查询参数代替路径参数 | `?name=xxx` | `/{name}` |

### 1.3 方法语义

| 操作 | HTTP 方法 | URL 示例 | curl 示例 |
|------|-----------|----------|-----------|
| 列表查询 | `GET` | `/api/stock-strategies` | `curl '/api/stock-strategies?page=1&size=20'` |
| 详情查询 | `GET` | `/api/stock-strategies/detail` | `curl '/api/stock-strategies/detail?name=xxx'` |
| 创建资源 | `POST` | `/api/stock-strategies` | `curl -X POST -H 'Content-Type: application/json' -d '{"name":"..."}'` |
| 触发操作 | `POST` | `/api/stock-strategies/regression` | `curl -X POST -d '{"name":"..."}'` |
| 保存配置 | `POST` | `/api/schedule-configs` | `curl -X POST -d '{...}'` |

### 1.4 关于 `.json` 后缀

**新接口不加 `.json` 后缀**。通过 `Accept: application/json` 和 `Content-Type: application/json` 控制内容类型。

```
# 新接口标准
GET  /api/stock-strategies           ✅
POST /api/schedule-configs           ✅

# 旧接口兼容（逐步迁移）
GET  /api/stock-strategy/list.json   ⏳ 遗留
```

### 1.5 为什么不用路径参数

```bash
# 路径参数 — curl 必须拼接 URL，参数含特殊字符时需编码
curl '/api/stock-strategies/macd_daily_v1/detail'

# 查询参数 — curl 直接传，参数顺序无关，AI 容易构造
curl '/api/stock-strategies/detail?name=macd_daily_v1'
```

路径参数对 curl 构造不够友好（需要 URL 拼接），对 AI 也不够显式。查询参数是 key=value 对，AI 可以自由控制哪些参数传、哪些不传。

---

## 2. 请求参数规范

### 2.1 GET 请求 — 查询参数

```java
@GetMapping
public ResponseDTO<List<StrategyDTO>> list(
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size) {
    // ...
}
```

对应 curl：
```bash
curl 'http://localhost:8080/api/stock-strategies?page=1&size=20'
```

**要点：**
- 参数名用 camelCase，与 Java 变量名保持一致
- 可为空的参数加上 `required = false`
- 提供合理的 `defaultValue`
- 枚举参数用字符串，不传数字序号
- 日期参数用 ISO 8601：`?startDate=2024-01-01`
- boolean 参数用 `true`/`false`，不用 `0`/`1`

### 2.2 POST/PUT 请求 — JSON Body

```java
@PostMapping
public ResponseDTO<Void> create(@RequestBody @Valid StrategyCreateRequest request) {
    // ...
}
```

对应 curl：
```bash
curl -X POST 'http://localhost:8080/api/stock-strategies' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "macd_daily_v2",
    "description": "MACD 日线策略 V2"
  }'
```

**Request DTO 设计要点：**
- 每个操作定义专用的 Request DTO，不复用 Entity/DO
- DTO 字段用 `@NotBlank`、`@NotNull`、`@Min`、`@Max` 等 Bean Validation 注解
- 字段名与 JSON key 一一对应（Jackson 默认，不额外配置）
- boolean 字段用 `Boolean` 包装类型（支持缺省）
- 枚举字段直接传字符串值（Jackson 自动反序列化）
- JSON 结构控制在 2 层以内，不深层嵌套

### 2.3 参数校验

使用 Bean Validation 注解：

```java
public class StrategyCreateRequest {
    @NotBlank(message = "策略名称不能为空")
    private String name;

    @Size(max = 200, message = "描述不能超过 200 字")
    private String description;
}
```

Controller 方法参数加 `@Valid`：

```java
@PostMapping
public ResponseDTO<Void> create(@RequestBody @Valid StrategyCreateRequest request) {
    // ...
}
```

---

## 3. 响应规范

### 3.1 统一响应体

沿用现有 `ResponseDTO<T>`：

```json
{
  "code": 200,
  "msg": "成功",
  "data": { ... }
}
```

**强约束：** 所有 Controller 方法必须返回 `ResponseDTO<T>`，禁止直接返回 `List<T>`、`T` 或 `ResponseEntity<T>`。

成功/失败的判断标准对 AI 来说就是 `code == 200`，简单明确。

### 3.2 分页响应

分页查询返回统一格式：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "items": [...],
    "total": 156,
    "page": 1,
    "size": 20
  }
}
```

建议新增 `PageResponseDTO<T>`：

```java
@Getter
@Setter
public class PageResponseDTO<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;
}
```

### 3.3 错误响应

```json
{
  "code": 40001,
  "msg": "策略名称不能为空",
  "data": null
}
```

**错误信息规范：**
- 说出具体问题：`"策略名称不能为空"` ✅ / `"参数错误"` ❌
- 包含上下文：`"股票代码 600001 不存在"` ✅ / `"数据未找到"` ❌
- 错误码 `200` = 成功，其他值 = 失败（AI 只需判断 `code == 200`）

### 3.4 日期和时间格式

- 日期：`yyyy-MM-dd` → `"2024-01-15"`
- 完整时间：`yyyy-MM-dd HH:mm:ss` → `"2024-01-15 14:30:00"`
- 在全局 Jackson 配置中统一设置：

```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
```

### 3.5 枚举输出

**枚举字段输出字符串值**，而非数字序号：

```json
{ "status": "BUY" }
```

而非：
```json
{ "status": 0 }     /* ❌ AI 需要额外映射才知道 0 代表买入 */
```

**新增枚举需遵循：**
- 枚举常量名大写（标准 Java 风格）
- 用 `@JsonValue` 标注输出字段（如果 displayName 与枚举名不同）
- 确保 Jackson 能正确序列化/反序列化（默认 `toString()` 或 `name()`）

---

## 4. 错误处理

### 4.1 全局异常处理器

现有代码每个 Controller 方法手动处理错误，导致大量重复判断。**必须改造为统一异常处理**：

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseDTO.error(40001, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseDTO<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseDTO.error(40002, "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusiness(BusinessException e) {
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleUnknown(Exception e) {
        log.error("未处理的异常", e);
        return ResponseDTO.error(50000, "系统内部错误，请稍后重试");
    }
}
```

**改造收益：**
- Controller 方法不再需要手动 `if(entity.isSuccess()) { ... } else { ... }` 结构
- 所有参数校验错误统一返回 40001，AI 能从 msg 字段直接读到具体问题
- 未捕获异常不会返回 500 堆栈信息，避免信息泄露

### 4.2 业务异常

```java
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}
```

用法：
```java
if (entity.isSuccess()) {
    return ResponseDTO.success(entity.getStrategyDetailDTO());
}
throw new BusinessException(context.getResultCode(), context.getResultMsg());
```

### 4.3 业务码规范

| 范围 | 含义 |
|------|------|
| `200` | 成功 |
| `40xxx` | 客户端错误（参数、校验、资源不存在） |
| `41xxx` | 鉴权/权限 |
| `42xxx` | 资源不存在 |
| `50xxx` | 服务端错误 |

---

## 5. Controller 编码规范

### 5.1 标准模板

```java
@RestController
@RequestMapping("/api/stock-strategies")
public class StockStrategyController {

    private final StockStrategyService stockStrategyService;

    // 构造器注入（必须）
    public StockStrategyController(StockStrategyService stockStrategyService) {
        this.stockStrategyService = stockStrategyService;
    }

    @GetMapping
    public ResponseDTO<List<StrategyDTO>> list(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        return ResponseDTO.success(stockStrategyService.list(page, size));
    }

    @GetMapping("/detail")
    public ResponseDTO<StrategyDetailDTO> detail(@RequestParam String name) {
        return ResponseDTO.success(stockStrategyService.detail(name));
    }

    @PostMapping
    public ResponseDTO<Void> create(@RequestBody @Valid StrategyCreateRequest request) {
        stockStrategyService.create(request);
        return ResponseDTO.success();
    }
}
```

### 5.2 具体要求

| 要求 | 说明 |
|------|------|
| 构造器注入 | 禁止 `@Resource` / `@Autowired` 字段注入 |
| Controller 不做业务 | 只做参数校验 + 调用 Service + 包装 ResponseDTO |
| 方法命名 | 反映 HTTP 方法：`list()`、`detail()`、`create()`、`delete()` |
| 冗余注入 | 只注入实际使用的依赖，删除未使用的注入字段 |

### 5.3 遗留代码迁移

现有 `StockStrategyController` 使用了 ProcessEngine 模式，该模式在短期内可以保留，但新增接口应遵循本规范的标准结构。

---

## 6. 认证与鉴权

### 6.1 API 无状态

- 不使用 HttpSession / Cookie 传递用户状态
- 使用 `Authorization: Bearer <token>` 标准 Header

```bash
curl -H 'Authorization: Bearer xxx' 'http://localhost:8080/api/stock-strategies'
```

### 6.2 Curl 兼容性

所有需要认证的接口，认证信息都通过 Header 传递，不依赖 Cookie。这样 curl 可以直接用 `-H 'Authorization: ...'` 调用，不需要 `-b`/`-c` 管理 Cookie。

---

## 7. Curl 友好检查清单

每个接口发布前对照检查：

- [ ] GET 请求能否用一条 `curl '<url>?param=value'` 直接调用？
- [ ] POST 请求能否用 `curl -X POST -H 'Content-Type: application/json' -d '{...}'` 清晰表达？
- [ ] URL 是否控制在 `/api/{资源}/{动作}` 以内？
- [ ] 是否不需要 Cookie / Session？
- [ ] 错误信息是否在响应体 JSON 中直接返回，而非依赖 HTTP 状态码之外的机制（如重定向、Header）？
- [ ] 响应 JSON 是否扁平（不超过 3 层嵌套）？
- [ ] 认证信息是否通过 `Authorization: Bearer` Header 传递？

### Curl 调用示例

```bash
# 1. 列表查询
curl 'http://localhost:8080/api/stock-strategies?page=1&size=20'

# 2. 详情查询
curl 'http://localhost:8080/api/stock-strategies/detail?name=macd_daily_v1'

# 3. 单股明细
curl 'http://localhost:8080/api/stock-strategies/stock-detail?name=macd_daily_v1&code=000001'

# 4. 创建回归任务（POST + JSON Body）
curl -X POST 'http://localhost:8080/api/stock-strategies/regression' \
  -H 'Content-Type: application/json' \
  -d '{"name": "macd_daily_v1"}'

# 5. 保存配置（POST + JSON Body）
curl -X POST 'http://localhost:8080/api/schedule-configs' \
  -H 'Content-Type: application/json' \
  -d '{"name": "daily_task", "cron": "0 0 9 * * ?"}'
```

---

## 8. AI 友好检查清单

- [ ] 从 URL 和参数名能否推断接口功能？（AI 不需要外部文档也能理解）
- [ ] 响应字段名是否自解释？（不用 `cnt`、`amt`、`info` 等模糊缩写）
- [ ] 枚举值是否用字符串输出？（AI 能直接读懂含义）
- [ ] 错误信息是否具体到"哪个字段、什么原因"？
- [ ] 日期时间是否全部使用 ISO 8601 格式？
- [ ] 分页参数是否统一使用 `page` / `size`？（AI 不需要为每个接口适配不同分页参数名）
- [ ] 响应结构是否统一？（所有接口返回 `ResponseDTO<T>`，AI 用同一套逻辑反序列化）
- [ ] HTTP 方法语义是否准确？（GET 不修改数据，POST 不用于纯查询）

---

## 9. 当前项目问题清单

以下是对当前代码的审计，新接口应避免同类问题：

| 问题 | 位置 | 建议 |
|------|------|------|
| `.json` 后缀 | 所有接口 | 新接口不加，旧接口逐步迁移 |
| 字段注入 `@Resource` | 所有 Controller | 改为构造器注入 |
| 无全局异常处理 | — | 新增 `@ControllerAdvice` |
| 冗余注入（同时注入接口和实现类） | `StockStrategyController` | 只注入需要的依赖 |
| 空 Controller 暴露路由 | `StockBasicListController` | 移除未使用的类 |
| 参数无校验 | 所有 `@RequestParam` | 添加 `@NotBlank` 等 |
| 错误码用 202/208 而非 HTTP 语义码 | `ErrorCode.java` | 改为 40xxx/50xxx 标准格式 |
