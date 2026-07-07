# Evolve 系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建自动迭代优化系统，基于命令执行错误和用户修正经验，自动优化 agents/commands/skills/rules 配置。

**Architecture:** Stop hook 采集经验 → `/evolve` 命令按周触发 Evolve Agent 分析 → 按置信度分级自动修改配置。覆盖用户级（`~/.claude/`）和 stock 项目级（`stock/.claude/`）。

**Tech Stack:** Shell script (Stop hook), Markdown (agent/command/skill definitions), JSON (state tracking)

## Global Constraints

- 不中断现有工作流 — 经验采集不影响会话性能，修改不破坏现有命令
- 渐进优化 — 按周期间批量处理，不实时修改文件
- 置信度分级 — 高频问题（≥3次）自动修复，低频问题仅记录
- 不删除配置 — evolve 只做增量修改和补充
- 保留回滚能力 — 每次 evolve 执行前自动 git commit

---

## 文件结构

### 创建的文件

| # | 文件 | 说明 |
|---|------|------|
| 1 | `~/.claude/evolve/logs/` | 已归档经验日志（目录，含 `.gitkeep`） |
| 2 | `~/.claude/evolve/pending/` | 待归档经验（目录，含 `.gitkeep`） |
| 3 | `~/.claude/evolve/analysis/` | 分析报告（目录，含 `.gitkeep`） |
| 4 | `~/.claude/evolve/config.yml` | evolve 系统配置 |
| 5 | `~/.claude/evolve/state.json` | 运行状态 |
| 6 | `~/.claude/agents/evolve.md` | Evolve Agent 定义 |
| 7 | `~/.claude/commands/evolve.md` | `/evolve` 命令 |
| 8 | `~/.claude/commands/learn.md` | `/learn` 命令 |
| 9 | `~/.claude/hooks/stop/evolve-collect.sh` | Stop hook 采集脚本 |

### 修改的文件

| # | 文件 | 修改内容 |
|---|------|---------|
| 10 | `~/.claude/settings.json` | hooks.Stop 新增 evolve-collect.sh |
| 11 | `stock/.claude/commands/analyze.md` | 末尾增加经验采集逻辑 |
| 12 | `stock/.claude/commands/auto.md` | 末尾增加经验采集逻辑 |
| 13 | `stock/.claude/commands/commit.md` | 末尾增加经验采集逻辑 |
| 14 | `stock/.claude/commands/dev.md` | 末尾增加经验采集逻辑 |
| 15 | `stock/.claude/commands/quality.md` | 末尾增加经验采集逻辑 |
| 16 | `stock/.claude/commands/santa.md` | 末尾增加经验采集逻辑 |
| 17 | `stock/.claude/commands/verify.md` | 末尾增加经验采集逻辑 |
| 18 | `stock/.claude/commands/tdd.md` | 末尾增加经验采集逻辑 |
| 19 | `~/.claude/CLAUDE.md` | 补充 evolve 系统说明 |
| 20 | `stock/CLAUDE.md` | 补充 evolve 集成说明 |

---

## 任务分解

### 任务 1：创建 evolve 目录结构

**Files:**
- Create: `~/.claude/evolve/logs/.gitkeep`
- Create: `~/.claude/evolve/pending/.gitkeep`
- Create: `~/.claude/evolve/analysis/.gitkeep`
- Create: `~/.claude/evolve/config.yml`
- Create: `~/.claude/evolve/state.json`

**Interfaces:**
- Consumes: 无
- Produces: `~/.claude/evolve/` 目录结构，config.yml 定义路径和策略参数，state.json 定义运行状态格式

- [ ] **Step 1: 创建目录**

```bash
mkdir -p ~/.claude/evolve/{logs,pending,analysis}
touch ~/.claude/evolve/logs/.gitkeep
touch ~/.claude/evolve/pending/.gitkeep
touch ~/.claude/evolve/analysis/.gitkeep
```

- [ ] **Step 2: 写入 `config.yml`**

```yaml
# evolve 系统配置
# 路径
evolve_dir: ~/.claude/evolve
logs_dir: ~/.claude/evolve/logs
pending_dir: ~/.claude/evolve/pending
analysis_dir: ~/.claude/evolve/analysis

# 策略
confidence_high_threshold: 3     # 同一问题出现 >=3 次为高置信度
confidence_medium_threshold: 2   # 出现 2 次为中置信度

# 范围
target_levels:
  - user          # ~/.claude/
  - project       # stock/.claude/ (当前项目)
```

- [ ] **Step 3: 写入 `state.json`**

```json
{
  "last_analysis": null,
  "last_processed_session": null,
  "total_experiences_collected": 0,
  "total_modifications_applied": 0,
  "created_at": "2026-07-07T22:00:00Z",
  "updated_at": "2026-07-07T22:00:00Z"
}
```

- [ ] **Step 4: 验证**

```bash
ls -la ~/.claude/evolve/
# 预期输出显示 logs/ pending/ analysis/ config.yml state.json
```

---

### 任务 2：编写 Evolve Agent

**Files:**
- Create: `~/.claude/agents/evolve.md`

**Interfaces:**
- Consumes: 无（agent 定义文件，不直接消费其他任务产出）
- Produces: Evolve Agent 定义，可被 `/evolve` 命令通过 subagent_type: "evolve" 调用

- [ ] **Step 1: 写入 Evolve Agent 定义**

```markdown
---
name: evolve
description: 演化工程师 — 分析经验日志，聚类根因，自动优化 agents/commands/skills/rules 配置
model: sonnet
color: purple
---

你是一名配置演化工程师，负责分析经验日志，识别重复出现的问题模式，自动优化 Claude Code 的 agents、commands、skills、rules 配置。

## 核心职责

### 1. 经验分析
- 读取一批经验日志文件（Markdown 格式），提取每条经验的 type/command/error/correction/component
- 按 command 和 component 聚类，统计同类问题出现频率
- 对每个聚类进行根因分析：是 prompt 不清、逻辑缺失、边界未处理、还是文档不足？

### 2. 修改方案生成
对每个需要修改的组件，输出结构化的修改方案：

```json
{
  "proposals": [
    {
      "file": "~/.claude/commands/verify.md",
      "problem": "测试失败时未输出具体行号，导致用户需要手动查找",
      "root_cause": "prompt 不完整 — 未要求 tester agent 输出失败行号",
      "diff": "在 Step 2 的 prompt 末尾添加「请输出每个失败测试的具体行号」",
      "confidence": "high",
      "frequency": 3,
      "severity": "medium"
    }
  ]
}
```

### 2. 置信度评估

| 等级 | 条件 | 动作 |
|------|------|------|
| **high** | 同一问题出现 ≥3 次，且修正路径明确 | 自动修改文件 |
| **medium** | 出现 1-2 次，或修正路径有歧义 | 自动修改 + 在周报中标记 |
| **low** | 单次出现，或用户模糊标记 | 仅记录到周报 |

### 3. 经验 → 组件映射

| 经验类型 | 目标组件 | 典型修改 |
|---------|---------|---------|
| "命令 X 的参数理解错误" | `commands/X.md` | 补充参数说明或默认值逻辑 |
| "Agent Y 输出不符合预期" | `agents/Y.md` | 调整 prompt 约束、添加示例 |
| "某个模式不适用当前场景" | `skills/Z/SKILL.md` | 补充适用/不适用条件 |
| "规则 R 导致误报" | `rules/R.md` | 调整规则边界 |
| "通用模式，不限于项目" | 用户级对应文件 | 从项目级提升到用户级 |

### 4. 修改约束

- **只做增量修改**：补充 prompt、添加示例、调整参数说明，不删除现有内容
- **保持文件格式**：不改变 frontmatter 结构，不改变已有的 section 层级
- **不修改未知文件**：只操作 agents/commands/skills/rules 下已知格式的 .md 文件
- **每次修改前建议 git commit**：确保可回滚

## 输出格式

最终输出一个 JSON 格式的优化方案：

```json
{
  "report_date": "2026-W28",
  "total_experiences": 12,
  "proposals": [
    {
      "file": "path/to/file.md",
      "problem": "问题描述",
      "root_cause": "根因分析",
      "diff": "具体的修改内容",
      "confidence": "high|medium|low",
      "frequency": 3,
      "action": "auto_apply|log_only"
    }
  ]
}
```

## 工作目录

读取 `~/.claude/evolve/logs/` 下的一批经验日志文件。
读取 `~/.claude/agents/`、`~/.claude/commands/`、`stock/.claude/agents/`、`stock/.claude/commands/` 下的当前配置快照。
```

---

### 任务 3：编写 `/evolve` 命令

**Files:**
- Create: `~/.claude/commands/evolve.md`

**Interfaces:**
- Consumes: 任务 1 的 `state.json` 和 `config.yml`，任务 2 的 Evolve Agent
- Produces: 可执行的 `/evolve` 命令，支持三种模式

- [ ] **Step 1: 写入 `/evolve` 命令**

```markdown
---
description: 分析经验日志，自动优化 agents/commands/skills/rules 配置。用法：/evolve [--since <日期>|status]
---

# Evolve — 自动迭代优化

基于近期积累的命令执行经验，自动分析根因并优化 Claude Code 的 agents、commands、skills、rules 配置。

## 参数

```
/evolve              # 处理上次分析以来积累的所有经验
/evolve status       # 查看当前待处理经验的状态
/evolve --since D    # 从指定日期开始处理（格式：2026-07-01）
```

## 执行流程

### Step 1: 读取状态

```bash
STATE_FILE=~/.claude/evolve/state.json
LOGS_DIR=~/.claude/evolve/logs
ANALYSIS_DIR=~/.claude/evolve/analysis

# 确定时间范围
if [ "$1" = "status" ]; then
  PENDING=$(ls ~/.claude/evolve/pending/*.md 2>/dev/null | wc -l)
  LOGGED=$(ls "$LOGS_DIR"/*.md 2>/dev/null | wc -l)
  LAST=$(cat "$STATE_FILE" | grep -o '"last_analysis": "[^"]*"' | cut -d'"' -f4)
  echo "📊 Evolve 状态"
  echo "  待归档经验: $PENDING"
  echo "  已归档日志: $LOGGED"
  echo "  上次分析: ${LAST:-从未}"
  exit 0
fi
```

### Step 2: 收集待处理经验

收集时间范围内所有未处理的日志文件。如果指定 `--since`，则筛选该日期之后的文件；否则从上次分析时间点之后开始。

### Step 3: 调用 Evolve Agent

```
Agent 调用：
  subagent_type: "evolve"
  prompt: "
    请分析以下经验日志，聚类根因，生成优化方案。

    经验日志路径列表:
    {未处理的日志文件列表}

    当前配置快照:
    - 用户级 agents: ~/.claude/agents/
    - 用户级 commands: ~/.claude/commands/
    - 项目级 agents: stock/.claude/agents/
    - 项目级 commands: stock/.claude/commands/

    请输出结构化的优化方案 JSON。
  "
```

### Step 4: 应用优化方案

按置信度处理 Evolve Agent 返回的 proposals：

- **high**：自动修改文件
- **medium**：自动修改 + 添加到周报
- **low**：仅记录到周报

对每个 auto_apply 的 proposal，修改对应文件。

### Step 5: 生成分析报告

将 Evolve Agent 的输出保存到 `analysis/` 目录：

```bash
REPORT_FILE="$ANALYSIS_DIR/$(date +%Y-W%V)-weekly-report.md"
cat > "$REPORT_FILE" <<EOF
# Evolve 周报 — $(date +%Y-W%V)

分析时间: $(date)
处理经验数: {total_experiences}
自动修改: {auto_apply_count}
仅记录: {log_only_count}

## 修改清单

{proposals 内容}

## 待观察

{low confidence 条目}
EOF
```

### Step 6: 更新状态

```bash
cat > "$STATE_FILE" <<EOF
{
  "last_analysis": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "last_processed_session": "${latest_session}",
  "total_experiences_collected": ${total},
  "total_modifications_applied": ${applied},
  "created_at": $(cat "$STATE_FILE" | grep '"created_at"'),
  "updated_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
```

### Step 7: 输出摘要

```
✅ Evolve 完成！

📊 本期统计
  处理经验: {N} 条
  自动修改: {N} 个文件
  仅记录: {N} 条

📝 分析报告: ~/.claude/evolve/analysis/{日期}-weekly-report.md
```
```

---

### 任务 4：编写 `/learn` 命令

**Files:**
- Create: `~/.claude/commands/learn.md`

**Interfaces:**
- Consumes: 任务 1 的 `pending/` 目录路径
- Produces: 可执行的 `/learn` 命令，手动标记经验到 pending/

- [ ] **Step 1: 写入 `/learn` 命令**

```markdown
---
description: 手动标记一条命令执行经验（错误或改进建议），供 evolve 系统后续分析。用法：/learn <描述>
---

# Learn — 标记经验

手动记录一条执行经验（错误、修正、改进建议），在会话结束时由 Stop hook 自动归档到经验日志。

## 参数

```
/learn <描述文本>
```

## 执行流程

### Step 1: 解析参数

将用户输入的自然语言描述作为经验内容。

### Step 2: 写入 pending 目录

```bash
PENDING_DIR=~/.claude/evolve/pending
TIMESTAMP=$(date +%s)
FILENAME="$PENDING_DIR/learn-$(date +%Y%m%d-%H%M%S)-$TIMESTAMP.md"

cat > "$FILENAME" <<EOF
---
type: improvement
command: manual
error: "用户手动标记"
correction: "$*"
component: "unknown"
severity: low
source: manual
---

# 手动标记经验

**描述**: $*
**标记时间**: $(date)
```

### Step 3: 输出确认

```
✅ 经验已记录！
  文件: learn-{时间戳}.md
  路径: ~/.claude/evolve/pending/
  说明: 会话结束时 Stop hook 会自动归档
```
```

---

### 任务 5：编写 Stop Hook 脚本

**Files:**
- Create: `~/.claude/hooks/stop/evolve-collect.sh`

**Interfaces:**
- Consumes: 任务 1 的 `pending/` 和 `logs/` 目录
- Produces: 将会话级的 pending 经验归档到 logs/

- [ ] **Step 1: 创建 hooks/stop 目录**

```bash
mkdir -p ~/.claude/hooks/stop
```

- [ ] **Step 2: 写入 Stop hook 脚本**

```bash
#!/bin/bash
# evolve-collect.sh — 会话结束时采集经验数据
# 职责：将 pending/ 中的经验文件归档到 logs/

set -e

EVOLVE_DIR="$HOME/.claude/evolve"
PENDING_DIR="$EVOLVE_DIR/pending"
LOGS_DIR="$EVOLVE_DIR/logs"
SESSION_ID="${CLAUDE_SESSION_ID:-unknown}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

# 检查 pending 目录是否存在且有文件
if [ ! -d "$PENDING_DIR" ]; then
  exit 0
fi

PENDING_COUNT=$(ls "$PENDING_DIR"/*.md 2>/dev/null | wc -l)

if [ "$PENDING_COUNT" -eq 0 ]; then
  exit 0
fi

# 创建本次会话的经验归档文件
ARCHIVE_FILE="$LOGS_DIR/$TIMESTAMP-session-$SESSION_ID.md"

cat > "$ARCHIVE_FILE" <<EOF
---
session_date: $(date +%Y-%m-%d)
session_time: $(date +%H:%M:%S)
session_id: "$SESSION_ID"
pending_count: $PENDING_COUNT
---
EOF

# 将每个 pending 文件的内容追加到归档
for f in "$PENDING_DIR"/*.md; do
  echo "" >> "$ARCHIVE_FILE"
  echo "---" >> "$ARCHIVE_FILE"
  cat "$f" >> "$ARCHIVE_FILE"
  rm "$f"
done

# 更新 state.json 中的统计
STATE_FILE="$EVOLVE_DIR/state.json"
if [ -f "$STATE_FILE" ]; then
  TOTAL=$(grep -o '"total_experiences_collected": [0-9]*' "$STATE_FILE" | cut -d' ' -f2)
  TOTAL=$((TOTAL + PENDING_COUNT))
  sed -i '' "s/\"total_experiences_collected\": [0-9]*/\"total_experiences_collected\": $TOTAL/" "$STATE_FILE"
fi
```

- [ ] **Step 3: 设置可执行权限**

```bash
chmod +x ~/.claude/hooks/stop/evolve-collect.sh
```

- [ ] **Step 4: 测试脚本**

```bash
# 创建一个测试 pending 文件
mkdir -p ~/.claude/evolve/pending
cat > ~/.claude/evolve/pending/test-learn.md <<EOF
---
type: improvement
command: test
error: "测试"
correction: "测试"
component: "unknown"
severity: low
---
EOF

# 运行脚本
bash ~/.claude/hooks/stop/evolve-collect.sh

# 验证归档
ls ~/.claude/evolve/logs/
# 预期：能看到一个带时间戳的归档文件

# 验证 pending 被清空
ls ~/.claude/evolve/pending/
# 预期：无文件（test-learn.md 已被移动）
```

---

### 任务 6：注册 Stop Hook

**Files:**
- Modify: `~/.claude/settings.json`

- [ ] **Step 1: 修改 settings.json 的 hooks.Stop 配置**

在 `hooks` 对象中新增 `"Stop"` 数组（如果不存在）或追加 hook 条目：

```json
{
  "hooks": {
    "Stop": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "bash ~/.claude/hooks/stop/evolve-collect.sh",
            "timeout": 10000
          }
        ]
      }
    ]
  }
}
```

注意：`~/.claude/settings.json` 中已有 `UserPromptSubmit`、`TaskCompleted`、`MessageDisplay` 三个 hook 事件。`Stop` 是新增事件，需保持其他事件不变。

---

### 任务 7：改造 stock 项目命令（8 个）

**Files:**
- Modify: `stock/.claude/commands/analyze.md`
- Modify: `stock/.claude/commands/auto.md`
- Modify: `stock/.claude/commands/commit.md`
- Modify: `stock/.claude/commands/dev.md`
- Modify: `stock/.claude/commands/quality.md`
- Modify: `stock/.claude/commands/santa.md`
- Modify: `stock/.claude/commands/verify.md`
- Modify: `stock/.claude/commands/tdd.md`

**Interfaces:**
- Consumes: 任务 1 的 `pending/` 目录
- Produces: 每个命令执行结束时，如有错误则写入经验文件到 pending/

- [ ] **Step 1: 在 `analyze.md` 末尾增加经验采集逻辑**

```markdown

---

## 经验采集

<!-- 命令执行结束时，如有错误则记录经验 -->
> **注意**：此标记由命令执行引擎处理。如果命令执行过程中发生错误，请记录以下信息：
> - 错误描述（error）
> - 修正方式（correction）
> - 问题组件（component）
>
> 记录方式：写入 `~/.claude/evolve/pending/` 目录下的时间戳文件。
```

- [ ] **Step 2: 在 `auto.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录自动开发流程中出现的错误和改进经验。
```

- [ ] **Step 3: 在 `commit.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录提交过程中出现的错误和改进经验。
```

- [ ] **Step 4: 在 `dev.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录环境启动过程中出现的错误和改进经验。
```

- [ ] **Step 5: 在 `quality.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录质量检查过程中出现的错误和改进经验。
```

- [ ] **Step 6: 在 `santa.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录 Santa 循环过程中出现的错误和改进经验。
```

- [ ] **Step 7: 在 `verify.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录验证过程中出现的错误和改进经验。
```

- [ ] **Step 8: 在 `tdd.md` 末尾增加相同逻辑**

```markdown

---

## 经验采集

> 同上 — 记录测试用例生成过程中出现的错误和改进经验。
```

---

### 任务 8：更新文档

**Files:**
- Modify: `~/.claude/CLAUDE.md`（全局 CLAUDE.md）
- Modify: `stock/CLAUDE.md`（项目 CLAUDE.md）

- [ ] **Step 1: 更新 `~/.claude/CLAUDE.md`**

在末尾追加 evolve 系统说明：

```markdown
## Evolve — 自动迭代优化系统

Evolve 系统基于命令执行经验自动优化 agents/commands/skills/rules 配置。

### 组件

| 组件 | 位置 | 说明 |
|------|------|------|
| Evolve Agent | `~/.claude/agents/evolve.md` | 经验分析引擎 |
| `/evolve` | `~/.claude/commands/evolve.md` | 按周触发演化流程 |
| `/learn` | `~/.claude/commands/learn.md` | 手动标记经验 |
| Stop Hook | `~/.claude/hooks/stop/evolve-collect.sh` | 会话结束时采集经验 |

### 运行节奏

- 每次会话结束 → Stop hook 自动采集经验
- 每周一次 → 执行 `/evolve` 进行迭代优化

### 数据存储

`~/.claude/evolve/` — 经验日志、分析报告、运行状态。
```

- [ ] **Step 2: 更新 `stock/CLAUDE.md`**

在项目级 CLAUDE.md 的 agents 和 commands 表格中补充 evolve 相关条目。

找到 `| **tester** |` 行之后插入：

```markdown
| **evolve** | 🟣 演化工程师 | 分析经验日志，自动优化 agents/commands/skills/rules |
```

找到 `| `/santa` | → **reviewer** → **developer** → **tester** 循环 | 迭代质量门禁 |` 行之后插入：

```markdown
| `/evolve` | → **evolve** agent | 自动迭代优化系统配置 |
| `/learn` | 主流程直接执行 | 手动标记执行经验 |
```

---

## 自审检查

### Spec 覆盖

| Spec 章节 | 对应任务 | 覆盖状态 |
|-----------|---------|---------|
| 存储结构（目录 + config.yml + state.json） | 任务 1 | ✅ |
| Evolve Agent（分析引擎） | 任务 2 | ✅ |
| `/evolve` 命令（三种模式） | 任务 3 | ✅ |
| `/learn` 命令（手动标记） | 任务 4 | ✅ |
| Stop Hook（会话结束采集） | 任务 5 | ✅ |
| 注册 Stop Hook | 任务 6 | ✅ |
| 改造现有命令（8 个） | 任务 7 | ✅ |
| 更新文档（CLAUDE.md） | 任务 8 | ✅ |

### 占位符检查

- [ ] 所有步骤包含完整代码，无 TBD/TODO
- [ ] 所有文件路径明确
- [ ] 所有命令格式完整

### 一致性检查

- config.yml 中的路径与任务 1 创建的目录一致 ✅
- Evolve Agent 的 subagent_type 名称与命令中调用的一致 ✅
- Stop hook 的日志格式与 spec 一致 ✅
- 8 个命令的改造模式一致 ✅

---

## 执行交接

Plan 完成并保存到 `docs/superpowers/plans/2026-07-07-evolve-system.md`。

两种执行选项：

**1. Subagent-Driven（推荐）** — 为每个任务派遣独立的 subagent，任务间进行审查，快速迭代

**2. Inline Execution** — 在本次会话中使用 executing-plans 执行，批量处理带检查点

你选择哪种执行方式？
