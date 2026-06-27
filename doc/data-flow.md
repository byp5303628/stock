# 数据流与业务流转

## 核心数据流

### 1. 策略详情查询

```
GET /api/stock-strategy/detail.json?name=MacdDailyV1TradePolicy

Controller: StockStrategyController
  │  构造 ProcessContext(productCode="stock_strategy", businessCode="detail")
  │  Entity = new StrategyDetailEntity(name="MacdDailyV1TradePolicy")
  ▼
ProcessExecutor.execute()
  │  加载 stock_strategy_detail.json 配置
  ▼
Stage: PROCESS
  ├─ GetTradePolicyByNameAction   → 从 TradePolicyFactory 获取策略 Bean
  ├─ GetStockCntAction            → 查询全量股票数量
  ├─ GetPolicyRegressionDetailAction → 获取策略回归历史数据
  ├─ CalculatePolicyDetailAction  → 计算统计指标（直方图等）
  └─ BuildStrategyIndicatorRespAction → 组装 StrategyDetailDTO 写入 Entity
  ▼
Controller: 从 Entity 取出 StrategyDetailDTO → ResponseDTO.success()
```

### 2. 创建回归任务

```
POST /api/stock-strategy/create-regression.json?name=MacdDailyV1TradePolicy

Controller: StockStrategyController
  │  ProcessContext(productCode="stock_strategy", businessCode="create_regression")
  ▼
ProcessExecutor.execute()
  │  加载 stock_strategy_create_regression.json 配置
  ▼
Stage: PROCESS
  ├─ GetTradePolicyByNameAction           → 获取策略 Bean
  ├─ ValidateTradePolicyRegressionExistsAction → 检查是否已有回归任务
  ├─ QueryAllStockCodeAction              → 获取所有股票代码列表
  └─ CreateTaskAndFlushRegressionAction   → 创建 Task，写入数据库
  ▼
TaskConsumer 异步消费任务
  │  TaskHandlerFactory.selectHandler("tradePolicyRegression")
  ▼
TradePolicyRegressionTaskHandler
  │  对每只股票执行 TradePolicy.trade(code)
  │  计算指标，汇总结果
  │  写入 trade_policy_regression + stock_regression_detail 表
  ▼
  Task.status = SUCCESS
```

### 3. 交易策略执行

```
TradePolicy.trade(code)
  │
  ├─ stockBasicDomainService.queryAllHfqStockBasics(code)  → 后复权K线
  ├─ stockStatisticsDomainService.queryStats(code, MACD)   → MACD统计数据
  ├─ stockStatisticsDomainService.queryStats(code, MONTH)  → 月度统计
  ├─ stockStatisticsDomainService.queryStats(code, DAY)    → 均价统计
  │
  └─ trade0(StockContext, TradeContext)
       │
       ├─ 遍历每条日K线
       │   ├─ 判断买卖信号（MACD金叉/死叉，均线突破等）
       │   ├─ TradeContext.purchase(stockBasic)  → 创建买入 TradeCycle
       │   └─ TradeContext.sale(stockBasic)      → 完成卖出
       │
       └─ 返回 TradeContext（含完整买卖周期列表）
```

## 对象转换链路

```
HTTP 请求
  │
  ├─ 查询参数 → Controller 直接取 @RequestParam
  └─ JSON Body → Jackson 反序列化为 DTO (@RequestBody + @Valid)
       │
Controller → ProcessEngine → Action 链
       │
       │  Action 内部调用 DomainService + DbConverter
       │  从 Mapper 取 DO → DbConverter.toDomain() → 领域模型
       ▼
  Entity.resultDTO  ← DtoConverter.toDto(domain)
       │
Controller → ResponseDTO<T>(dto)
       │
Jackson 序列化 → JSON 响应
```

## 领域模型 ↔ DB 模型转换

```
DB 表               DO 类 (stock-common)    领域模型 (stock-core)
────────────────── ──────────────────────── ──────────────────────────
qfq/hfq_stock_basic StockBasicDO            StockBasic (remote层)
stock_statistics    StockStatisticsDO        StockStatistics
trade_policy_       TradePolicyRegressionDO  TradePolicyRegression
 regression
stock_regression_   StockRegressionDetailDO  StockRegressionDetail
 detail
task                TaskDO                   Task
schedule_config     ScheduleConfigDO         ScheduleConfig

          DbConverter (stock-core/converter/)
          DO ──────────────────────────────→ Domain
          DbConverter.toDomain(do) / toDO(domain)
```

## 任务系统状态机

```
INIT → PROCESSING → SUCCESS
      ↘        ↘
         → FAIL (可重试 → RETRY → PROCESSING)
```

- `TaskConsumer` 负责消费任务
- `TaskHandlerFactory` 按 `taskType` 路由到具体 Handler
- Handler 类型：`HfqHistoryRegressionTaskHandler`、`TradePolicyRegressionTaskHandler`、`HistoryStrategyTaskHandler`

## 元数据系统

元数据系统提供动态数据模型管理能力：

- **MetadataModel** — 模型定义（类似"表"的抽象）
- **MetadataField** — 字段定义（类似"列"的抽象，含类型/校验规则）
- **MetadataEnum** / **MetadataEnumValue** — 枚举定义
- **EnumRefDetail** — 字段引用枚举的关系
- **ValidateRequest** — 数据校验（按模型定义校验数据）

相关 Controller：`MetadataController`、`MetadataIntegrationController`（校验/绑定/解绑）。
