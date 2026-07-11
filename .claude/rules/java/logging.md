# Java 日志规范

> 本规则扩展 [coding-style.md](../common/coding-style.md)，定义日志记录的统一标准。

## 1. Logger 声明 — 使用 Lombok @Slf4j

### 1.1 强制规则

**禁止手动创建 Logger，统一使用 Lombok `@Slf4j` 注解。**

```java
// ✅ 正确
@Slf4j
@Controller
public class FinancialDataController {
    public void someMethod() {
        log.info("操作成功: code={}", code);
    }
}

// ❌ 错误 — 手动创建 Logger
public class FinancialDataController {
    private static final Logger log = LoggerFactory.getLogger(FinancialDataController.class);
    // ...
}
```

### 1.2 适用范围

所有需要日志的类（Controller、Service、Component、TaskHandler、Action 等）：

```java
@Slf4j
@Component
public class RouteDispatcher { ... }

@Slf4j
@Service
public class GenericDataDomainServiceImpl { ... }
```

---

## 2. 日志级别

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| `error` | 不可恢复的错误、异常 | `log.error("数据库写入失败: dataType={}, code={}", dataType, code, e)` |
| `warn` | 可恢复的异常、非预期状态 | `log.warn("模型已存在，跳过: {}", code)` |
| `info` | 关键业务流程的开始/结束 | `log.info("元数据初始化完成")` |
| `debug` | 开发期调试信息，上线后关闭 | `log.debug("SQL 执行耗时: {}ms", cost)` |
| `trace` | 极少使用，仅在追踪线上问题时临时开启 | — |

---

## 3. 日志内容规范

### 3.1 占位符替换

**使用 SLF4J 占位符 `{}`，禁止字符串拼接：**

```java
// ✅ 正确 — 占位符，懒加载
log.info("处理完成: code={}, type={}", code, type);

// ❌ 错误 — 字符串拼接，即使日志级别不输出也执行拼接
log.info("处理完成: code=" + code + ", type=" + type);
```

### 3.2 异常日志

异常作为最后一个参数传递，不手动调用 `e.getMessage()`：

```java
// ✅ 正确
try {
    // ...
} catch (Exception e) {
    log.error("处理失败: code={}", code, e);  // e 会自动输出完整堆栈
}

// ❌ 错误 — 只输出了 message 没有堆栈
log.error("处理失败: " + e.getMessage());
```

### 3.3 上下文信息

日志中必须包含足够的上下文信息，方便定位问题：

```java
// ✅ 正确 — 包含业务键
log.info("查询完成: dataType={}, code={}, market={}, total={}",
        dataType, code, market, total);

// ❌ 错误 — 无上下文
log.info("查询完成");
```

---

## 4. 禁止行为

| 禁止项 | 说明 |
|--------|------|
| `System.out.println` | 必须替换为 SLF4J |
| `System.err.println` | 必须替换为 SLF4J |
| `e.printStackTrace()` | 必须替换为 `log.error(..., e)` |
| 打印敏感信息 | 密码、token、身份证号等必须脱敏 |
| 无意义的 info 日志 | 循环内的每条记录处理不要打 info，用 debug |
| 日志内容不含上下文 | 见 3.3 |

---

## 5. 检视清单

- [ ] 使用 `@Slf4j` 而非手动创建 Logger？
- [ ] 使用 `{}` 占位符而非字符串拼接？
- [ ] 异常日志将 Throwable 作为最后一个参数？
- [ ] 日志含足够的上下文信息？
- [ ] 无 `System.out.println` / `e.printStackTrace()`？
- [ ] 日志级别使用正确？（error/warn/info/debug）
