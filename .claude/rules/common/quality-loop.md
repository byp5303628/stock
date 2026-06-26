---
paths:
  - "**/*.java"
  - "**/*Mapper.xml"
  - "**/*.properties"
---
# Quality 自愈循环

> 本规则扩展 `/quality` 命令的行为，增加自动修复 + 多轮验证。

## 流程

```
round = 0
max_rounds = 3
while round < max_rounds:
    round += 1
    1. 运行 quality check（静态分析 + 安全扫描）
    2. 收集 HIGH + MEDIUM 问题
    3. 如果没有问题 → 退出循环
    4. 自动修复所有 HIGH + MEDIUM 问题
    5. mvn compile 验证编译通过
    6. 进入下一轮
```

## 问题自动修复规则

| 问题类型 | 修复方式 | 安全等级 |
|----------|----------|----------|
| `System.out.println` | 替换为 `log.info()` / `log.warn()` | ✅ 安全 |
| MyBatis `${}` 用于 int 参数 | 改为 `#{}` | ✅ 安全 |
| `Resource` 字段注入 | 改为构造器注入 + `private final` | ⚠️ 需谨慎 |
| `Autowired` 字段注入 | 改为构造器注入 + `private final` | ⚠️ 需谨慎 |
| `java.util.Date` | 替换为 `java.time.LocalDateTime` | ❌ 手动修复（影响面广） |

### 字段注入 → 构造器注入 自动转换规则

```java
// 原代码
@Resource
private SomeService someService;
@Resource
private OtherService otherService;

// 自动转换后
private final SomeService someService;
private final OtherService otherService;

public XxxController(SomeService someService, OtherService otherService) {
    this.someService = someService;
    this.otherService = otherService;
}
```

**限制条件：**
- 只修复 Controller 类（避免影响 Action/TaskHandler 等框架类）
- 单文件内所有 `@Resource`/`@Autowired` 一起转换
- 不修复存在循环依赖的文件

## 三轮退出条件

任一轮检查后 **HIGH=0 且 MEDIUM=0** → 立即退出，标记为 PASS。

如果 3 轮后仍有残留问题：
- HIGH 残留 → 标记 FAIL，需人工介入
- 仅 MEDIUM 残留 → 标记 PASS_WITH_WARN
