---
description: 分析需求文档（飞书链接或本地文件），产出方案设计文档 + TODO 任务拆分 + 测试用例（TDD 就绪），上传飞书。用法：/analyze <飞书链接|本地文件路径>
---

# Analyze — 需求分析、方案设计与测试用例生成

通过 **architect agent** 进行需求分析与方案设计，随后自动串联 **testcase-generator agent** 生成测试用例，一次产出三份文档，直接用于 TDD 开发。

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
  ├─ Step 2: → architect agent ← 方案设计 + TODO
  │    │
  │    ├─ spec/feature-{序号}/方案设计-{需求名称}.md（Spec 模板）
  │    └─ spec/feature-{序号}/TODO-{需求名称}.md
  │
  ├─ Step 3: → testcase-generator agent ← 测试用例 ← 新的串联步骤
  │    │
  │    └─ spec/feature-{序号}/测试用例-{需求名称}.md
  │
  ├─ Step 4: 上传飞书归档（仅飞书链接场景）
  │
  └─ Step 5: 输出摘要
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
         - 使用 Spec 模板（含背景与目标、方案设计、备选方案、测试计划、迁移发布、影响面分析）
         - 测试计划章节必须详细到接口级别，便于后续测试用例生成

      2. TODO 拆分 → spec/feature-{序号}/TODO-{需求名称}.md
         - 按阶段拆分，每任务 0.5~2 人天
         - 给出明确验收条件和涉及文件列表
  "
```

### Step 3: 派发给 testcase-generator agent 生成测试用例

读取上一步产出的方案设计文档，派发给 **testcase-generator** agent：

```
Agent 调用：
  subagent_type: "testcase-generator"
  prompt: "
    方案设计文档: {方案设计文档内容}
    工作目录: spec/feature-{序号}/

    请基于方案设计文档生成测试用例：
    → spec/feature-{序号}/测试用例-{需求名称}.md

    要求：
    - 覆盖方案设计中「测试计划」章节列出的全部接口和场景
    - 每个接口至少 3 个用例：正常 + 边界 + 异常
    - curl 命令完整可执行
    - 包含端到端场景
    - 测试数据使用唯一值
  "
```

### Step 4: 上传飞书归档（仅飞书链接场景）

调用 **lark-wiki** skill 将三份文档作为子文档上传留档。

### Step 5: 输出摘要

汇总以下信息：

```
## 产出清单

| 文档 | 路径 |
|------|------|
| 方案设计 | spec/feature-{序号}/方案设计-{需求名称}.md |
| 任务拆分 | spec/feature-{序号}/TODO-{需求名称}.md |
| 测试用例 | spec/feature-{序号}/测试用例-{需求名称}.md |

## 核心决策（来自方案设计）
- ...

## 任务概览
| 阶段 | 任务数 | 总预估 |
|------|--------|--------|
| ... | ... | ... |

## 测试概览
| 维度 | 数量 |
|------|------|
| 接口测试数 | ... |
| 端到端场景数 | ... |

## 下一步
- 运行 `/tdd spec/feature-{序号}/方案设计-{需求名称}.md` 重新生成测试用例
- 运行 `/dev backend` 启动后端开始开发
```
