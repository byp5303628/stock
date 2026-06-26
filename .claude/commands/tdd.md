---
description: 根据方案设计文档生成可执行的测试用例，含接口测试（curl + 预期返回）和端到端测试（步骤化执行指引）。用法：/tdd <方案设计文档路径>
---

# TDD — 测试用例生成

通过 **testcase-generator agent** 根据方案设计文档生成完整可执行的测试用例。

## 参数

```
/tdd <方案设计文档路径>
```

## Agent 编排

```
主流程（Main）
  │
  ├─ Step 1: 读取方案设计文档（主流程执行）
  │
  └─ Step 2: → testcase-generator agent ← 核心工作
       │
       ├─ 分析接口定义、模型、业务规则
       ├─ 生成接口测试用例（curl + 预期）
       └─ 生成端到端测试用例（步骤化）
```

## 执行流程

### Step 1: 读取方案设计文档

读取指定的方案设计文档，提取接口定义、模型定义、执行路径、业务规则等。

### Step 2: 派发给 testcase-generator agent

启动 **testcase-generator** agent，传入方案设计内容，要求产出测试用例文档：

```
Agent 调用：
  subagent_type: "testcase-generator"
  prompt: "
    方案设计文档: {文档内容}
    请产出测试用例文档 → {同目录}/测试用例-{需求名称}.md
  "
```

### Step 3: 输出摘要

输出测试概览：接口数、用例数、端到端场景数、覆盖模块。
