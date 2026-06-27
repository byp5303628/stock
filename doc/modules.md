# 模块详解

## 依赖关系

```
stock-web ──→ stock-biz ──┬──→ stock-core ──→ stock-common
                           │        ↓
                           │   stock-remote
                           │
                           └──→ stock-remote
```

## stock-web — 启动入口

**职责**：Spring Boot 应用启动 + 架构守卫测试。

```
stock-web/src/main/java/.../web/
  └─ WebStarter.java          # @SpringBootApplication 入口
stock-web/src/test/java/.../web/
  └─ ArchitectureTest.java    # ArchUnit 架构规则
```

无业务代码，只做装配和启动。

## stock-biz — 业务逻辑中心

**职责**：所有业务逻辑，包括 Controller、流程引擎、交易策略、任务系统、指标计算。

### 核心包结构

```
stock-biz/src/main/java/com/ethanpark/stock/biz/
  ├─ controller/          # REST API 控制器
  │   ├─ StockStrategyController    # 策略查询/回归任务
  │   ├─ ScheduleConfigController   # 调度配置 CRUD
  │   ├─ MetadataController         # 元数据管理
  │   └─ MetadataIntegrationController  # 元数据集成（校验/绑定）
  │
  ├─ engine/              # ★ 流程引擎核心
  │   ├─ ProcessContext           # 流程上下文（黑板）
  │   ├─ ProcessExecutor          # 引擎执行器接口
  │   ├─ ProcessConfig            # 流程配置接口
  │   ├─ ProcessConfigImpl        # 配置实现（从 JSON 加载）
  │   ├─ ProcessStage             # 阶段（含 actions 列表+router）
  │   ├─ Action                   # @Action 注解
  │   ├─ BusinessAction           # Action 接口 (process)
  │   ├─ Router                   # 路由接口 + 阶段常量
  │   ├─ entity/BaseEntity        # Entity 基类
  │   ├─ exception/ProcessException # 流程异常
  │   ├─ config/
  │   │   └─ ProcessConfigCacheImpl  # 流程配置缓存
  │   └─ impl/
  │       └─ ProcessExecutorImpl  # 引擎实现
  │
  ├─ process/             # 流程相关的 Action + Entity
  │   ├─ actions/               # Action 实现
  │   │   ├─ GetTradePolicyByNameAction      # 获取交易策略
  │   │   ├─ GetStockCntAction              # 获取股票数量
  │   │   ├─ GetPolicyRegressionDetailAction # 获取回归详情
  │   │   ├─ CalculatePolicyDetailAction    # 计算策略指标
  │   │   ├─ BuildStrategyIndicatorRespAction # 构建响应
  │   │   ├─ QueryAllStockCodeAction        # 查询所有股票代码
  │   │   ├─ ValidateTradePolicyRegressionExistsAction # 验证回归是否存在
  │   │   ├─ CreateTaskAndFlushRegressionAction # 创建任务+刷新
  │   │   └─ CommonErrorHandleAction       # 通用错误处理
  │   └─ entity/               # Entity（数据载体）
  │       ├─ StrategyDetailEntity          # 策略详情实体
  │       ├─ StockStrategyDetailEntity     # 单股明细实体
  │       └─ StrategyDetailRegressionEntity # 回归任务实体
  │       └─ aware/              # Entity 契约接口
  │           ├─ TradePolicyAware
  │           ├─ StockCntAware
  │           ├─ StockRegressionDetailsAware
  │           └─ StockCodeListAware
  │
  ├─ trade/               # 交易策略
  │   ├─ TradePolicy              # 策略接口
  │   ├─ TradePolicyFactory       # 策略工厂（自动收集所有实现）
  │   └─ impl/
  │       ├─ BaseTradePolicy      # 策略基类（加载K线+统计数据）
  │       ├─ MacdDailyV1TradePolicy ~ V4  # MACD 日线策略（4个版本）
  │       └─ AverageV1TradePolicy, V2     # 均线策略（2个版本）
  │
  ├─ cal/                 # 指标计算
  │   ├─ StatisticsStrategy       # 统计维度策略接口
  │   └─ StatisticsStrategyFactory # 统计策略工厂
  │
  ├─ task/                # 异步任务系统
  │   ├─ TaskConsumer             # 任务消费入口
  │   ├─ TaskHandler              # 任务处理器接口
  │   ├─ TaskHandlerFactory       # 处理器工厂（按 taskType 路由）
  │   └─ TaskLoader               # 任务加载
  │
  ├─ converter/           # DTO 转换器
  │   └─ DtoConverter
  │
  ├─ handler/             # 全局异常处理
  │   └─ GlobalExceptionHandler   # @ControllerAdvice
  │
  ├─ dto/                 # 数据传输对象
  │   ├─ ResponseDTO<T>          # 统一响应体
  │   ├─ StrategyDTO             # 策略摘要
  │   ├─ StrategyDetailDTO       # 策略详情
  │   ├─ TradeCycleDTO           # 买卖周期
  │   ├─ StockRegressionDetailDTO # 回测明细
  │   ├─ ScheduleConfigDTO       # 调度配置
  │   └─ Metadata*/Enum*         # 元数据相关 DTO
  │
  ├─ config/              # Biz 模块配置
  └─ exception/           # 业务异常
      ├─ BusinessException        # 通用业务异常
      └─ ProcessException         # 流程异常（engine内）
```

### 关键设计决策

**为什么 Controller 不直接调用 Service？**

传统 Spring MVC：Controller → Service → Repository。本项目：Controller → ProcessEngine → Action 链。因为核心业务（策略详情、回归任务）是多步骤流程，不是简单 CRUD，用流程引擎管理步骤间的依赖和流转比在 Service 里写长方法更清晰。

## stock-core — 领域模型

**职责**：领域模型 + Domain Service 接口。不含任何数据库操作代码，只定义接口。

```
stock-core/src/main/java/com/ethanpark/stock/core/
  ├─ model/               # 领域模型
  │   ├─ TradeContext          # 交易上下文（含买卖周期列表+报表生成）
  │   ├─ TradeCycle            # 单个买卖周期
  │   ├─ TradeLog              # 单次交易记录（买入/卖出）
  │   ├─ TradeBehavior         # 交易行为枚举（PURCHASE/SALE）
  │   ├─ StockContext          # 股票分析上下文（K线+统计数据）
  │   ├─ StockStatistics       # 统计指标
  │   ├─ StockRegressionDetail # 单股回归明细
  │   ├─ StatisticsType        # 统计维度枚举（MACD/MONTH_STAT/DAY_STAT）
  │   ├─ MacdStat              # MACD 统计值
  │   ├─ RegressionDetail      # 回归分析详情
  │   ├─ StockPredictIndicator # 预测指标
  │   ├─ Task / TaskStatus     # 任务 + 状态枚举
  │   ├─ ScheduleConfig        # 调度配置
  │   └─ StockAttribute        # 股票属性
  │   └─ metadata/             # 元数据模型
  │       ├─ MetadataModel/Field/Enum/EnumValue  # 元数据定义
  │       └─ EnumUsage/ValidationResult           # 枚举使用+校验
  │
  ├─ service/             # Domain Service 接口（只定义，不实现）
  │   ├─ StockBasicDomainService           # 股票基础数据
  │   ├─ StockStatisticsDomainService      # 统计数据
  │   ├─ StockRegressionDomainService      # 回归数据
  │   ├─ TradePolicyRegressionDomainService # 策略回归数据
  │   ├─ StockInfoDomainService            # 股票信息
  │   ├─ TaskDomainService                 # 任务管理
  │   ├─ ScheduleConfigDomainService       # 调度配置
  │   ├─ MetadataDomainService             # 元数据管理
  │   └─ IndicatorDomainService            # 指标计算
  │       └─ impl/MetadataDomainServiceImpl  # Metadata 唯一有实现的
  │
  └─ converter/            # 领域对象转换
      ├─ DbConverter       # DO ↔ Domain 转换
      └─ DomainConverter   # Domain ↔ Domain 转换
```

### 模型关系

```
TradePolicy.trade(code) → TradeContext
  ├─ code
  ├─ strategyDesc
  ├─ tradeCycles[]                # 买卖周期列表
  │   ├─ purchaseLog (TradeLog)   # 买入记录
  │   └─ saleLog (TradeLog)       # 卖出记录
  └─ 报表生成 (genReport)
```

## stock-common — 数据访问层

**职责**：MyBatis Mapper + DO + 工具类。

```
stock-common/src/main/java/com/ethanpark/stock/common/
  ├─ dal/
  │   ├─ general/StockBasicMapper      # 通用 Mapper（MyBatis XML 绑定）
  │   └─ mappers/                      # 注解式 Mapper
  │       ├─ entity/                   # DO 类（与表一一对应）
  │       │   ├─ StockBasicDO          # qfq/hfq_stock_basic 表
  │       │   ├─ StockStatisticsDO     # stock_statistics 表
  │       │   ├─ StockRegressionDetailDO
  │       │   ├─ TradePolicyRegressionDO
  │       │   ├─ TaskDO
  │       │   ├─ ScheduleConfigDO
  │       │   ├─ MetadataModelDO
  │       │   ├─ MetadataFieldDO
  │       │   ├─ MetadataEnumDO
  │       │   └─ MetadataEnumValueDO
  │       └─ Mapper 接口               # @Select/@Insert/@Update
  │           ├─ QfqStockBasicMapper
  │           ├─ HfqStockBasicMapper
  │           ├─ StockStatisticsMapper
  │           ├─ StockRegressionDetailMapper
  │           ├─ TradePolicyRegressionMapper
  │           ├─ TaskMapper
  │           ├─ ScheduleConfigMapper
  │           ├─ MetadataModelMapper
  │           ├─ MetadataFieldMapper
  │           ├─ MetadataEnumMapper
  │           └─ MetadataEnumValueMapper
  │
  ├─ config/StockDalConfig  # MyBatis 配置
  └─ util/
      ├─ DateUtils           # 日期工具
      ├─ JsonUtils           # JSON 工具
      └─ MathUtils           # 数学工具
```

### DO 隔离规则

`StockBasicDO` 等标注 `@Table` 的 DO 类**绝对不能出现在 Web 层**。数据必须在 Core 层通过 `DbConverter` 转换为领域模型后，才能流入 Biz 层和 Web 层。此规则由 ArchUnit 的 `DO实体不能流入Web层做返回` 测试守卫。

## stock-remote — 外部 API 客户端

**职责**：封装对腾讯股票接口的 HTTP 调用。

```
stock-remote/src/main/java/com/ethanpark/stock/remote/
  ├─ HistoryStockClient    # 日K线数据（前复权/后复权）
  ├─ F10StockClient        # 股票基本信息
  └─ model/StockBasic      # 外部 API 数据模型
```

数据源：`web.ifzq.gtimg.cn`
- `https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param={prefix}{code},day,{start},{end},500,qfq`（前复权）
- `https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param={prefix}{code},day,{start},{end},500,hfq`（后复权）

**架构注意**：当前 Biz 层存在直接依赖 Remote 层的情况（42 处），这是已知的历史遗留违规，ArchUnit 的对应规则已被注释。长期应通过 Core 层包装 Remote 调用。

## stock-integration-test — 集成测试

**职责**：跨模块集成测试。

```
stock-integration-test/src/test/java/.../it/
  ├─ TestConfig.java      # 测试配置（Testcontainers MySQL）
  ├─ StockStrategyIT.java # 策略相关接口集成测试
  └─ MetadataIT.java      # 元数据接口集成测试
```

## 前端 (stock-frontend)

基于 Ant Design Pro + UmiJS 4 + React 18。

### 路由结构

```
/welcome                                    # 欢迎页
/strategy/strategy-list                     # 策略列表页
/strategy/strategy-list/strategy-detail     # 策略详情页
  └─ stock-strategy-detail                  # 单股明细页
/operation/schedule-config-list             # 调度配置管理
/operation/metadata-model-list              # 元数据模型列表
  ├─ model-editor                           # 模型编辑器
  └─ model-detail                           # 模型详情
/operation/metadata-enum-list               # 枚举管理
```

### 核心服务

```javascript
// src/services/strategy.js
getStockStrategyList()          → GET  /api/stock-strategy/list.json
getStockStrategyByName(name)    → GET  /api/stock-strategy/detail.json?name=
startTradeRegression(name)      → POST /api/stock-strategy/create-regression.json?name=
getStockPredictIndicator(data)  → GET  /api/stock-strategy/stock-detail.json?code=&name=
```
