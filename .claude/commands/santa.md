---
description: Santa 质量门禁循环 — 迭代执行 verify → scan → fix → verify，直至全部通过或达到上限。用法：/santa [max-rounds]
---

# Santa — 质量门禁循环

Santa 是迭代质量门禁循环，通过 **reviewer agent**（扫描）→ **developer agent**（修复）→ **tester agent**（验证）的循环收敛代码质量问题。

Santa = **S**can → **A**nalyze → **N**eutralize → **T**est → **A**ccept

## 参数

```
/santa [max-rounds]
```

| 参数 | 说明 |
|------|------|
| `max-rounds` | 最大迭代轮数（默认 5） |

## Agent 编排

```
Santa 循环（每轮）
  │
  ├─ Scan 阶段     → reviewer agent
  │                  扫描代码质量问题
  │
  ├─ Analyze 阶段  → 主流程
  │                  排序问题优先级，选择本轮要修复的
  │
  ├─ Fix 阶段      → developer agent
  │                  原子化修复选中的问题
  │
  └─ Verify 阶段   → tester agent
                     验证编译 + 测试是否通过
                     通过 → 继续下一轮
                     失败 → 回退重试
```

## 执行流程

### Round N

#### Step 1: Scan — 启动 reviewer agent

```
Agent 调用：
  subagent_type: "reviewer"
  prompt: "请全量扫描当前代码，输出 CRITICAL/HIGH/MEDIUM 问题清单"
```

#### Step 2: Analyze

从问题清单选择本轮要修复的问题：优先 CRITICAL，每次只修一个。

#### Step 3: Fix — 启动 developer agent

```
Agent 调用：
  subagent_type: "developer"
  prompt: "请修复以下问题（最小化修改）: {问题描述}"
```

#### Step 4: Verify — 启动 tester agent

```
Agent 调用：
  subagent_type: "tester"
  prompt: "编译+测试验证，确认修复未引入新问题"
```

#### Step 5: 判断

| 条件 | 动作 |
|------|------|
| Scan 无问题 | ✅ 跳出循环，输出总结 |
| Round >= max-rounds | ⏹ 输出中间结果 |
| 仍有问题 | 🔄 进入下一轮 |
