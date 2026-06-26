---
description: 分析 git 变更生成 commit message，提交并推送。用法：/commit ["自定义消息"]
---

# Commit — 智能提交

分析当前 git diff，自动生成 conventional commit message，提交并推送。

## 参数

```
/commit ["自定义消息"]
```

| 参数 | 说明 |
|------|------|
| 无参数 | 自动分析变更生成消息 |
| `"自定义消息"` | 跳过生成，直接使用自定义消息提交 |

## 执行流程

### Step 1: 暂存变更

```bash
git add -A
```

自动暂存所有变更（新增、修改、删除），确保 diff 完整。

### Step 2: 分析变更内容

读取 `git diff --cached --stat` 和 `git diff --cached` 分析：

- 新增了哪些文件
- 修改了哪些文件
- 删除了哪些文件
- 变更的核心意图

### Step 3: 生成 commit message

根据变更内容按以下规则确定 type 和 message：

| 变更特征 | type | 示例 |
|----------|------|------|
| 新增 Java class/interface | `feat` | `feat: add BusinessException and GlobalExceptionHandler` |
| 修改业务逻辑 | `feat` / `fix` | `fix: handle null pointer in StrategyDetailEntity` |
| 重命名/重构 | `refactor` | `refactor: change field injection to constructor injection` |
| 新增/修改测试 | `test` | `test: add assertion for MACD calculation` |
| 修改 pom.xml / 配置 | `chore` | `chore: add application-prod.properties` |
| 修改 .md / 文档 | `docs` | `docs: add API design standard rules` |
| 修复 bug | `fix` | `fix: correct offset calculation in pagination` |

### 消息格式

```
<type>: <简要变更说明>

<详细说明 — 列出主要变更点>
```

**message 质量要求：**
- 标题 ≤ 72 字符，用祈使句
- body 用项目符号列出关键变更，每个文件一个要点
- 不写"修改了什么"，写"做了什么"（e.g. "add validation" ✅ / "modified file" ❌）

### Step 4: 展示并确认

展示生成的 message，询问用户是否确认。用户可选择：
- 直接确认 → 执行 commit + push
- 修改 message → 编辑后继续
- 取消 → 放弃提交

### Step 5: 提交

```bash
git commit -m "<title>" -m "<body>"
git push
```

首次推送新分支自动加 `-u`。

## 示例

```
➜ /commit

📊 分析变更...
  - 4 个文件修改
  - 2 个文件新增
  - 涉及: stock-biz, stock-common

📝 生成 commit message:
  refactor: replace field injection with constructor injection in controllers

  - Refactor StockStrategyController to use constructor injection
  - Refactor ScheduleConfigController to use constructor injection
  - Clean up redundant processConfigCacheImpl injection
  - Clean up empty StockBasicListController

确认提交? [Y/n/edit]
```
