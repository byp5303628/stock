---
description: 代码质量检查：静态分析、代码规范、安全扫描、优化建议。用法：/quality [check|fix|report]
---

# Quality — 代码质量

通过 **reviewer agent** 对项目代码进行全面质量分析。

## 参数

```
/quality [check|fix|report]
```

| 参数 | 说明 |
|------|------|
| `check` | 执行质量检查，输出问题清单（默认值） |
| `fix` | 执行检查并自动修复可修复的问题 |
| `report` | 生成质量报告，保存到本地文件 |

## Agent 编排

```
主流程（Main）
  │
  └─ → reviewer agent ← 核心工作
       │
       ├─ 静态代码分析（代码规范、空指针、异常处理）
       ├─ 安全扫描（SQL 注入、硬编码密钥、XSS）
       ├─ 架构合规检查
       ├─ [fix 模式] 修复可自动修复的问题
       └─ [report 模式] 生成质量报告文件
```

## 执行流程

### Step 1: 派发给 reviewer agent

启动 **reviewer** agent 执行质量分析：

```
Agent 调用：
  subagent_type: "reviewer"
  prompt: "
    模式: {check|fix|report}
    请对项目全量代码进行质量审查
  "
```

### Step 2: 问题分级输出

agent 按以下级别输出问题：

| 级别 | 说明 | 处理要求 |
|------|------|---------|
| 🔴 CRITICAL | 安全漏洞 / 功能缺陷 | 合并前必须修复 |
| 🟡 HIGH | 规范违规 / 性能隐患 | 建议修复 |
| 🔵 MEDIUM | 可读性 / 重复代码 | 视情况修复 |
| 🟢 INFO | 潜在优化点 | 无需立即处理 |

### Step 3: 输出质量报告

按模式输出结果：`check` 输出问题清单；`fix` 执行修复；`report` 保存到 `quality-report.md`。
