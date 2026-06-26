---
description: 分析需求文档（飞书链接或本地文件），产出方案设计文档和 TODO 任务拆分并上传飞书。用法：/analyze <飞书链接|本地文件路径>
---

# Analyze — 需求分析与方案设计

通过 **architect agent** 对需求进行完整分析，产出方案设计文档和 TODO 任务拆分。

## 参数

```
/analyze <飞书文档链接|本地文件路径>
```

## Agent 编排

```
主流程（Main）
  │
  ├─ Step 1: 读取需求文档（主流程执行）
  │
  └─ Step 2: → architect agent ← 核心工作
       │
       ├─ 分析需求 → 产方案设计文档
       └─ 产出 TODO 任务拆分文档
```

## 执行流程

### Step 1: 读取需求文档

1. 判断参数类型：
   - 飞书链接 → 调用 **lark-doc** skill 读取文档内容 + **lark-drive** 读取评论
   - 本地文件 → 调用 `Read` 工具读取内容
2. 推断需求名称，创建 `spec/feature-{序号}/` 工作目录

### Step 2: 派发给 architect agent 进行方案设计

启动 **architect** agent，传入需求内容 + 项目上下文，请其产出两份文档：

```
Agent 调用：
  subagent_type: "architect"
  prompt: "
    项目: 股票策略回测系统（Spring Boot + React）
    需求文档: {需求内容}
    工作目录: spec/feature-{序号}/
    请产出：
      1. 方案设计文档 → spec/feature-{序号}/方案设计-{需求名称}.md
      2. TODO 拆分    → spec/feature-{序号}/TODO-{需求名称}.md
  "
```

agent 完成的工作：
1. **方案设计文档** — 需求概述、模型设计、架构设计、关键逻辑、影响面分析
2. **TODO 任务拆分** — 按阶段拆分的可执行任务列表，含依赖和验收条件

### Step 3: 上传飞书归档（仅飞书链接场景）

调用 **lark-wiki** skill 将方案设计/TODO 文档作为子文档上传留档。

### Step 4: 输出摘要

汇总产出文档路径、核心设计决策和任务概览。
