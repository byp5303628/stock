---
description: 运行项目验证：编译、测试、构建检查。用法：/verify [backend|frontend|full]
---

# Verify — 项目验证

通过 **tester agent** 和 **reviewer agent** 协同完成项目验证。

## 参数

```
/verify [backend|frontend|full]
```

| 参数 | 说明 |
|------|------|
| `backend` | 仅验证后端（Maven compile + test） |
| `frontend` | 仅验证前端（lint + test + build） |
| `full` | 后端 + 前端全量验证（默认值） |

## Agent 编排

```
主流程（Main）
  │
  ├─ → tester agent
  │    ├─ 运行编译检查
  │    ├─ 运行全部测试
  │    ├─ 运行架构守卫
  │    └─ 输出测试报告
  │
  └─ → reviewer agent（可选）
       └─ 审查待提交的变更
```

## 执行流程

### Step 1: 编译检查

```bash
mvn clean compile -DskipTests
```

如果编译失败 → 启动 **build-fix** 修复流程。

### Step 2: 派发给 tester agent 运行测试

启动 **tester** agent 执行测试并分析结果：

```
Agent 调用：
  subagent_type: "tester"
  prompt: "
    验证范围: {backend|frontend|full}
    请执行测试并输出验证报告
  "
```

### Step 3: 代码审查（full 模式）

启动 **reviewer** agent 审查 `git diff`：

```
Agent 调用：
  subagent_type: "reviewer"
  prompt: "请审查当前的 git diff 变更"
```

### Step 4: 输出验证报告

汇总编译、测试、架构守卫、代码审查的结果。
