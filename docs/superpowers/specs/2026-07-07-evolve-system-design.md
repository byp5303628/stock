# Evolve 系统设计 — 自动迭代优化 Agents/Commands/Skills/Rules

> 创建日期：2026-07-07
> 状态：设计稿

## 概述

基于执行命令的错误经验和用户的修正经验，自动迭代优化 Claude Code 配置（agents、commands、skills、rules），覆盖用户级（`~/.claude/`）和当前 stock 项目级（`stock/.claude/`）。

## 核心原则

- **不中断现有工作流** — 经验采集不影响会话性能，修改不破坏现有命令
- **渐进优化** — 按周期间批量处理，不实时修改
- **置信度分级** — 高频问题自动修复，低频问题仅记录
- **自我进化** — evolve 系统本身也纳入迭代范围

## 架构总览

```
┌──────────────────────────────────────────────────────────┐
│ ① 经验采集层（每次会话结束时触发）                        │
│   Stop Hook → 归档 pending/ 经验 → 写入 logs/           │
│                          │                                │
│ ② 分析层（按周触发 /evolve 命令）                        │
│   /evolve → Evolve Agent → 聚类分析 → 生成修改方案        │
│                          │                                │
│ ③ 应用层（自动修改配置）                                 │
│   高置信度 → 自动应用                                     │
│   中/低置信度 → 记录到周报                                 │
└──────────────────────────────────────────────────────────┘
```

## 存储结构

所有 evolve 相关文件存放在用户级 `~/.claude/evolve/`：

```
~/.claude/evolve/
├── logs/                  # 已归档的经验日志（每会话一个文件）
│   └── 2026-07-07-session-abc.md
├── pending/               # 待归档的经验（由 Stop hook 处理）
├── analysis/              # Evolve Agent 产出的分析报告
│   └── 2026-W28-weekly-report.md
├── config.yml             # evolve 系统配置
└── state.json             # 运行状态
```

### 经验日志格式

```markdown
---
session_date: 2026-07-07
duration_min: 45
session_id: "abc123"
---
## 经验记录

### 1. 命令执行错误
- **type**: error
- **command**: /verify
- **error**: 测试失败时错误信息不够清晰，没有输出失败行号
- **correction**: 在 verify 命令末尾添加了失败用例的详细输出
- **component**: commands/verify.md
- **severity**: medium

### 2. 使用改进
- **type**: improvement
- **command**: /commit
- **context**: commit message 过长，包含不必要的细节
- **correction**: 调整了 commit 命令的 message 模板，只保留 type: description
- **component**: commands/commit.md
- **severity**: low
```

## 组件设计

### 1. Evolve Agent（用户级）

**位置**: `~/.claude/agents/evolve.md`
**模型**: sonnet（分析任务需要较高质量）
**角色**: 专职分析经验日志，生成并应用配置优化方案

核心职责：
- 读取并聚类经验日志
- 识别同类问题的根因
- 生成针对 agents/commands/skills/rules 的修改 diff
- 按置信度分级输出

分析流程：
```
Input: 经验日志集合 + 当前配置快照
  ↓
聚类：将相似经验分组（同一命令、同一组件、同类问题）
  ↓
根因分析：问题是 prompt 不清、逻辑缺失、边界未处理？
  ↓
生成补丁：对每个待修改文件产出 diff 方案
  ↓
置信度评估：出现频率 × 修正明确性
  ↓
Output: 结构化修改方案列表
```

### 2. `/evolve` 命令（用户级）

**位置**: `~/.claude/commands/evolve.md`
**作用**: 触发进化流程

```bash
/evolve              # 处理过去一周的经验
/evolve status       # 查看待处理经验状态
/evolve --since D    # 指定起始日期
```

命令流程：
```
/evolve
  → 读取 state.json，定位未处理的时间范围
  → 收集 logs/ 中未处理的经验文件
  → 调用 Evolve Agent 分析
  → 按置信度应用修改
  → 生成分析报告 → 写入 analysis/
  → 更新 state.json
```

### 3. `/learn` 命令（用户级）

**位置**: `~/.claude/commands/learn.md`
**作用**: 手动标记一条经验

```bash
/learn verify 命令的测试失败日志输出不清晰，我手动添加了行号
```

执行后立即写入 `pending/` 目录，由 Stop hook 在会话结束时统一归档。

### 4. Stop Hook

**位置**: `~/.claude/hooks/stop/evolve-collect.sh`
**注册**: 在 `~/.claude/settings.json` 的 `hooks.Stop` 数组中添加

职责：
1. 扫描 `pending/` 目录，所有文件添加时间戳后移入 `logs/`
2. 清理空的 pending 目录
3. 不阻塞会话关闭流程（轻量 shell 脚本）

## 置信度分级策略

| 等级 | 条件 | 动作 |
|------|------|------|
| **高** | 同一问题出现 ≥3 次，且修正路径明确 | 自动修改文件 |
| **中** | 出现 1-2 次，或修正路径有歧义 | 自动修改 + 在周报中标记 |
| **低** | 单次出现，或用户模糊标记 | 仅记录到周报 |

## 现有命令的改造模式

在每个命令文件末尾增加经验采集逻辑（不改变原有行为）：

```markdown
<!-- 命令末尾增加 -->
[ -n "$ERROR_INFO" ] && cat > ~/.claude/evolve/pending/$(date +%s).md <<EOF
---
type: error
command: verify
error: "$ERROR_SUMMARY"
correction: "$CORRECTION"
component: "commands/verify.md"
severity: medium
---
EOF
```

需要改造的 stock 项目命令列表：
- `commands/analyze.md`
- `commands/auto.md`
- `commands/commit.md`
- `commands/dev.md`
- `commands/quality.md`
- `commands/santa.md`
- `commands/verify.md`
- `commands/tdd.md`

## 与现有系统的集成

### 需要创建的文件

| 文件 | 位置 | 说明 |
|------|------|------|
| `evolve.md` | `~/.claude/agents/` | Evolve Agent 定义 |
| `evolve.md` | `~/.claude/commands/` | /evolve 命令 |
| `learn.md` | `~/.claude/commands/` | /learn 命令 |
| `evolve-collect.sh` | `~/.claude/hooks/stop/` | Stop hook 脚本 |
| `config.yml` | `~/.claude/evolve/` | evolve 配置 |
| `state.json` | `~/.claude/evolve/` | 运行状态 |

### 需要修改的文件

| 文件 | 修改内容 |
|------|---------|
| `~/.claude/settings.json` | hooks.Stop 数组新增 evolve-collect.sh |
| `stock/.claude/commands/*.md` | 8 个命令末尾增加经验采集逻辑 |
| `~/.claude/CLAUDE.md` | 补充 evolve 系统说明 |
| `stock/CLAUDE.md` | 补充 evolve 集成说明 |

## 初始搭建步骤

1. 创建 `~/.claude/evolve/` 目录及子目录
2. 编写 Evolve Agent 定义文件
3. 编写 `/evolve` 命令
4. 编写 `/learn` 命令
5. 编写 Stop hook 脚本
6. 注册 Stop hook 到 settings.json
7. 改造 stock 项目下 8 个命令
8. 更新 CLAUDE.md 文档

## 运行节奏

```
初始搭建完成后：

每次会话结束 → Stop hook 自动采集经验
每周一次     → 手动执行 /evolve 进行迭代优化
每月一次     → 检查 evolve 自身是否需要优化
```

## 边界与约束

- **不分析敏感信息**：经验日志只记录命令名称和错误摘要，不记录代码内容
- **不修改未知文件**：只修改 agents/commands/skills/rules 下已知格式的 .md 文件
- **不删除配置**：evolve 只做增量修改和补充，不删除现有配置
- **保留回滚能力**：每次 evolve 执行前自动 commit，可回滚
