---
description: 分析 git 变更自动生成 commit message，直接提交并推送。用法：/commit ["自定义消息"]
---

# Commit — 自动提交

分析当前 git diff，自动生成 conventional commit message，直接提交并推送，无需人工确认。

## 参数

```
/commit ["自定义消息"]
```

| 参数 | 说明 |
|------|------|
| 无参数 | 自动分析变更，生成 message，直接 commit + push |
| `"自定义消息"` | 跳过生成，直接使用自定义消息提交 |

## 执行流程

### Step 1: 暂存变更

```bash
git add -A
```

自动暂存所有变更（新增、修改、删除）。

### Step 2: 分析变更内容

读取 `git diff --cached --stat` 和 `git diff --cached` 分析：

- 新增了哪些文件、修改了哪些文件、删除了哪些文件
- 按目录归类（Java / 配置 / 测试 / 文档 / 前端）
- 提取每个文件的关键意图

### Step 3: 生成 commit message

按以下规则自动确定 type 和 message：

| 变更特征 | type | 示例 |
|----------|------|------|
| 新增 Java class/interface | `feat` | `feat: add BusinessException and GlobalExceptionHandler` |
| 修改业务逻辑 | `feat` / `fix` | `fix: handle null pointer in StrategyDetailEntity` |
| 重命名/重构 | `refactor` | `refactor: change field injection to constructor injection` |
| 新增/修改测试 | `test` | `test: add assertion for MACD calculation` |
| 修改 pom.xml / 配置 | `chore` | `chore: add application-prod.properties` |
| 修改 .md / 文档 | `docs` | `docs: add API design standard rules` |
| 修复 bug | `fix` | `fix: correct offset calculation in pagination` |

当同时包含多种类型时，按优先级选取主要 type（feat > fix > refactor > test > chore > docs）。

**消息格式：**
```
<type>: <简要变更说明>

- <主要变更1>
- <主要变更2>
- ...
```

**质量要求：**
- 标题 ≤ 72 字符，用祈使句
- body 用项目符号列出关键变更，按文件/模块归类
- 不写"修改了什么"，写"做了什么"（`add validation` ✅ / `modified file` ❌）

### Step 4: 直接提交

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
  - 涉及模块: stock-biz, stock-common

📝 提交信息:
  refactor: replace field injection with constructor injection in controllers

  - Refactor StockStrategyController to use constructor injection
  - Refactor ScheduleConfigController to use constructor injection
  - Clean up redundant processConfigCacheImpl injection
  - Clean up empty StockBasicListController

✅ commit 62475be → main, pushed
```
