# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

股票交易策略回测分析系统，支持从公开数据源获取日K线数据，应用多种交易策略进行回测，生成统计指标和可视化报表。

- **后端**: JDK 17 + Spring Boot 2.5.6 + Maven 多模块
- **前端**: React 18 + Ant Design Pro (UmiJS 4)
- **数据库**: MySQL (MyBatis)
- **数据源**: 腾讯股票接口 (web.ifzq.gtimg.cn)

## 构建与运行

### 环境要求

- **JDK 17**（默认 JDK 是 26，本工程使用 JDK 17，需通过 Homebrew 管理多版本）
- **Node.js >= 12**
- **MySQL 8.0+**

```bash
# ★ JDK 17 环境设置（必须）
#   本工程使用 JDK 17，当前系统默认 JDK 为 26。
#   推荐在 ~/.zshrc 中设置以下环境变量，打开新终端后生效：

# 方案 A：通过 PATH + JAVA_HOME 设置 JDK 17（推荐）
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"

# 方案 B：仅设置 PATH（Maven 会优先使用 PATH 中的 java）
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

#   或在当前会话临时切换：
export JAVA_HOME=/opt/homebrew/opt/openjdk@17 && export PATH=$JAVA_HOME/bin:$PATH
```

### 后端 (Maven)

```bash
# 编译全部模块
mvn clean install -DskipTests

# 运行单个测试
mvn test -pl stock-web -Dtest=IntegrationTest#testWeb

# 启动后端服务 (入口类: stock-web/.../WebStarter.java)
# 在 IDE 中运行 WebStarter.main()，或：
mvn spring-boot:run -pl stock-web
```

### 前端 (stock-frontend)

```bash
cd stock-frontend

# 安装依赖
npm install

# 开发模式启动 (默认端口 8000)
npm start

# 无 mock 模式 (连接真实后端)
npm run start:no-mock

# 构建
npm run build

# 测试
npm test
```

## 后端架构

### Maven 模块依赖关系

```
stock-web (启动入口)
  └─ stock-biz (业务逻辑、控制器、流程引擎、交易策略)
       ├─ stock-core (领域模型、Domain Service)
       │    └─ stock-common (MyBatis Mapper、DO、工具类)
       └─ stock-remote (外部数据源客户端)
```

### 流程引擎 (Process Engine)

核心执行架构 (`com.ethanpark.stock.biz.engine`):

- **ProcessContext** — 上下文载体，包含 productCode/businessCode/entity/processConfig
- **ProcessConfig** — 按 productCode+businessCode 配置的执行阶段 (ProcessStage) 列表
- **ProcessStage** — 包含多个 Action bean 名称和 Router
- **Action** — 标注 `@Action` 的业务执行单元，实现 `BusinessAction` 接口
- **ProcessExecutor** — 引擎入口，按阶段依次执行 Action 列表
- **Router** — 控制阶段流转 (`PRE_PROCESS` → `PROCESS` → `POST_PROCESS` → `FINISH`)

Controller 标准写法：
```java
ProcessContext context = new ProcessContext();
context.setProductCode("stock_strategy");
context.setBusinessCode("detail");
StrategyDetailEntity entity = new StrategyDetailEntity();
context.setEntity(entity);
context.setProcessConfig(processConfigCache.getProcessConfig(context));
processExecutor.execute(context);
```

### 交易策略 (Trade Policy)

`com.ethanpark.stock.biz.trade`:

- **TradePolicy** 接口: `trade(code)` 返回 `TradeContext`，包含买卖周期列表
- 实现类: `MacdDailyV1-V4TradePolicy`, `AverageV1TradePolicy` 等
- 通过 `TradePolicyFactory` 自动收集所有 `TradePolicy` Bean

### 任务系统

- `TaskConsumer` — 消费任务，按状态机流转：INIT → PROCESSING → SUCCESS/FAIL
- `TaskHandlerFactory` — 根据 `taskType` 路由到对应 Handler
- Handler: `HfqHistoryRegressionTaskHandler`, `HistoryStrategyTaskHandler`, `TradePolicyRegressionTaskHandler`

### 指标计算

`com.ethanpark.stock.biz.cal.factory`:

- **StrategyIndicatorCalculator** 层级: base 计算器 (avg/cnt/max/min/percent) → 具体指标 (IncreaseTotalAvg, GoldCycleMax, YearIncreaseG0Cnt 等)
- **IndicatorFactory** — 计算器工厂
- **StatisticsStrategy** — 统计维度策略 (MACD/月统计/均价统计)

### 数据模型 (stock-core)

| 模型 | 说明 |
|------|------|
| `TradePolicyRegression` | 策略回归结果 (含 RegressionDetail) |
| `StockRegressionDetail` | 单只股票的策略回归明细 |
| `TradeContext` | 一次策略执行的上下文，含买卖周期列表 |
| `TradeCycle` | 一个完整买卖周期 (买入/卖出日志) |
| `StockPredictIndicator` | 预测指标 |
| `StockStatistics` | 统计数据 |
| `Task` / `ScheduleConfig` | 任务调度 |

### 数据源客户端 (stock-remote)

- `HistoryStockClient` — 获取日K线数据 (前复权/后复权)
- `F10StockClient` — 获取股票基本信息
- 接口地址: `web.ifzq.gtimg.cn`

### 数据库表 (MySQL)

核心表: `qfq_stock_basic`, `hfq_stock_basic`, `stock_info`, `stock_statistics`, `trade_policy_regression`, `stock_regression_detail`, `task`, `schedule_config`

详见 `basic_init.sql`。

### 后端 API

| 端点 | 说明 |
|------|------|
| `GET /api/stock-strategy/list.json` | 策略列表 |
| `GET /api/stock-strategy/detail.json?name=` | 策略详情 |
| `GET /api/stock-strategy/stock-detail.json?name=&code=` | 单股策略回测明细 |
| `POST /api/stock-strategy/create-regression.json?name=` | 发起策略回归任务 |
| `GET /api/schedule-config/list.json` | 调度配置列表 |
| `POST /api/schedule-config/save.json` | 保存调度配置 |

## 前端架构 (stock-frontend)

基于 Ant Design Pro + UmiJS 4 + React 18。

- **路由**: `/welcome` | `/strategy/strategy-list` → `strategy-detail` → `stock-strategy-detail` | `/operation/schedule-config-list`
- **服务层**: `src/services/strategy.js` (调用后端 API)
- **页面组件**: `src/pages/` 下按路由名组织
- **配置**: `config/config.js` (路由、代理等)

## 项目级 Agents

定义在 `.claude/agents/` 中，通过 `Agent` 工具按 `subagent_type` 调用：

| Agent | 角色 | 职责 |
|-------|------|------|
| **architect** | 🟢 架构师 | 需求分析、方案设计、TODO 拆分 |
| **developer** | 🔵 开发工程师 | 编码实现（Java/React），TDD 模式 |
| **testcase-generator** | 🟣 测试设计 | 生成接口测试和端到端测试用例 |
| **tester** | 🟠 测试验证 | 运行测试、分析结果、验证验收条件 |
| **reviewer** | 🔴 审查员 | 代码审查、安全审查、质量分析 |

## 自定义命令

命令通过派发任务给不同 agent 协作完成：

| 命令 | 调用 Agent | 说明 |
|------|-----------|------|
| `/rich <飞书文档链接>` | 主流程直接执行 | 读取飞书 PRD，语义润色后写回 |
| `/analyze <飞书链接\|本地文件>` | → **architect** | 需求分析 → 方案设计 + TODO 拆分 |
| `/tdd <方案设计路径>` | → **testcase-generator** | 方案设计 → 接口测试 + E2E 测试用例 |
| `/dev [backend\|frontend\|db\|all]` | 主流程直接执行 | 启动本地开发环境 |
| `/verify [backend\|frontend\|full]` | → **tester** + **reviewer** | 测试验证 + 代码审查 |
| `/quality [check\|fix\|report]` | → **reviewer** | 代码质量分析 + 3 轮自愈修复 |
| `/commit ["自定义消息"]` | 主流程直接执行 | 分析 diff 生成 message，commit + push |
| `/santa [max-rounds]` | → **reviewer** → **developer** → **tester** 循环 | 迭代质量门禁 |

Agent 协作详情见 `rules/common/agents.md`。

## 项目规则

- `rules/common/feishu-doc.md` — 读取飞书文档时必须同时读取评论（适用所有命令）

## 架构守卫

使用 ArchUnit 守护项目架构，规则定义在 `stock-web/.../ArchitectureTest.java`：

| 级别 | 规则 | 说明 |
|------|------|------|
| **P0** | 分层依赖 | Common → Core → Biz → Web，下层不得依赖上层 |
| **P0** | 无循环依赖 | 包之间禁止循环引用 |
| **P0** | DO 不流入 Web 层 | `@Entity`/`@Table` 标注的类不出现在 Web 层 |
| **P1** | Action 必须实现 BusinessAction | `@Action` 标注的类必须实现对应接口 |
| **P1** | Controller 命名 | 必须以 `Controller` 结尾，方法返回 `ResponseDTO` |
| **P1** | DomainService 命名 | Core 层 Service 接口必须以 `DomainService` 结尾 |
| **P2** | 代码规范 | 禁止 `System.out.println`，使用 Slf4j |

```bash
# 单独运行架构测试
mvn test -pl stock-web -Dtest=ArchitectureTest
```
