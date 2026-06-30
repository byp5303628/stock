# Agent 编排体系

本项目的开发流程通过多个专用 agent 协同完成，每个 agent 有明确的职责边界。

## 项目级 Agents

定义在 `.claude/agents/` 中，可通过 `Agent` 工具按 `subagent_type` 调用：

| Agent | 角色 | 职责 |
|-------|------|------|
| **architect** | 🟢 架构师 | 需求分析、方案设计、TODO 拆分、影响面评估 |
| **developer** | 🔵 开发工程师 | 编码实现（Java/React）、TDD 模式 |
| **testcase-generator** | 🟣 测试设计 | 生成接口测试和端到端测试用例 |
| **tester** | 🟠 测试验证 | 运行测试、分析结果、验证验收条件 |
| **reviewer** | 🔴 审查员 | 代码审查、安全审查、质量分析 |

## Agent 调用方式

```markdown
Agent 调用：
  subagent_type: "architect" | "developer" | "testcase-generator" | "tester" | "reviewer"
  prompt: "{明确的任务描述}"
```

## 命令 → Agent 映射

| 命令 | 调用的 Agent | 说明 |
|------|-------------|------|
| `/analyze` | **architect** → **testcase-generator** | 需求分析 → 方案设计 + TODO + 测试用例（三件套） |
| `/tdd` | **testcase-generator** | 方案设计 → 测试用例 |
| `/dev [backend\|frontend\|all]` | 主流程直接执行 | 启动本地环境，不依赖 agent |
| `/verify [backend\|frontend\|full]` | **tester** + **reviewer** | 测试验证 + 代码审查 |
| `/quality [check\|fix\|report]` | **reviewer** | 代码质量分析 + 修复 |
| `/santa [max-rounds]` | **reviewer** → **developer** → **tester** 循环 | 迭代质量门禁 |

## Agent 协作模式

### 串行依赖（开发新功能）

```
主流程 → architect（方案） → developer（实现） → tester（验证） → reviewer（审查）
```

### 并行执行（独立任务）

```markdown
并行启动：
  1. developer agent — 实现 {功能 A}
  2. developer agent — 实现 {功能 B}
  完成后：启动 tester agent 统一验证
```

### 迭代循环（Santa 质量门禁）

```
Santa 循环：
  reviewer agent（扫描） → 主流程（分析） → developer agent（修复） → tester agent（验证）
```

## Agent 使用原则

1. **单次只做一件事** — 每个 agent 调用聚焦一个明确任务
2. **传递充分上下文** — 提供相关文件路径、代码片段、需求文档
3. **验证输出** — agent 返回结果后由主流程校验
4. **异常处理** — agent 执行失败时，主流程分析原因后重试或升维处理
