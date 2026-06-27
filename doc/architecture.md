# 项目架构设计

## 项目概述

股票交易策略回测分析系统。从腾讯股票公开接口获取日 K 线数据，应用多种交易策略（MACD、均线等）进行历史回测，生成统计指标和可视化报表。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 2.5.6 (JDK 17) |
| 构建工具 | Maven 多模块 |
| 数据库 | MySQL 8.0 + MyBatis |
| 连接池 | Druid |
| HTTP 客户端 | Unirest |
| 序列化 | Fastjson |
| 前端 | React 18 + Ant Design Pro (UmiJS 4) |
| 架构测试 | ArchUnit |

## 设计哲学

### 1. 声明式流程编排（核心）

业务逻辑不是写在单体 Service 方法中，而是通过 **流程引擎 (Process Engine)** 将业务分解为小的 Action，用 JSON 配置文件编排执行顺序。

```
Controller → ProcessContext(productCode, businessCode) → ProcessExecutor → 按 JSON 配置执行 Action 链
```

**设计动机**：
- 复杂业务流程（策略详情查询、回归任务创建）涉及多个步骤（查策略→查股票→算指标→组装结果），用 Service 方法写会成为巨长的方法
- Action 是原子的、可复用的业务单元，不同流程可以共用
- JSON 配置文件让流程编排可视化，新增流程不需要写 Java 代码

### 2. 领域驱动分层

```
stock-web       (启动入口，Spring Boot 应用)
  └─ stock-biz  (业务逻辑、流程引擎、交易策略、控制器)
       ├─ stock-core  (领域模型、Domain Service 接口)
       │    └─ stock-common (MyBatis Mapper、DO、工具类)
       └─ stock-remote  (外部数据源客户端)
```

依赖方向：上层依赖下层，下层对上层无感知。由 ArchUnit 在编译期守卫。

### 3. 策略模式 — 交易策略可扩展

通过 `TradePolicy` 接口抽象交易策略，所有实现类由 Spring 自动收集到 `TradePolicyFactory`。新增策略只需写一个实现类并注册为 Bean。

### 4. Repository 模式 — 数据访问抽象

Core 层定义 Domain Service 接口（如 `StockBasicDomainService`），Common 层提供 Mapper 实现。Biz 层只依赖接口，不感知存储细节。

## 核心概念

### ProcessContext — 流程的"黑板"

整个流程共享一个 ProcessContext 实例，包含：
- `productCode` / `businessCode` — 路由键，决定执行哪个流程配置
- `entity` — 业务实体（数据载体 + 结果容器）
- `stage` — 当前执行阶段
- `resultCode` / `resultMsg` — 执行结果（替代异常传播）

### Entity — 既做输入也做输出

流程的 Entity 不是贫血模型，而是包含业务数据 + 执行结果的富对象：
- 实现 Aware 接口声明需要什么数据（如 `TradePolicyAware`）
- 流程执行后填充结果字段（如 `strategyDetailDTO`）
- 通过 `isSuccess()` 标记流程是否成功

### 流程配置（JSON）

流程定义在 `resources/process/` 下的 JSON 文件中：

```json
{
  "processStageMap": {
    "PROCESS": {
      "actions": ["getTradePolicyByNameAction", "getStockCntAction", ...]
    },
    "ERROR_PROCESS": {
      "actions": ["commonErrorHandleAction"]
    }
  }
}
```

文件名格式：`{productCode}_{businessCode}.json`，引擎启动时加载并缓存。

## 模块职责

| 模块 | 职责 | 关键内容 |
|------|------|----------|
| **stock-web** | 启动入口 + 架构测试 | `WebStarter.java`, `ArchitectureTest.java` |
| **stock-biz** | 业务逻辑中心 | Controller、流程引擎、Action、TradePolicy、Task 系统、指标计算 |
| **stock-core** | 领域模型 + 服务接口 | `TradeContext`, `TradeCycle`, `StockRegressionDetail`, `*DomainService` |
| **stock-common** | 数据访问层 | MyBatis Mapper、DO 类、工具类 |
| **stock-remote** | 外部 API 客户端 | `HistoryStockClient`, `F10StockClient` |

## 架构守卫 (ArchUnit)

编译期自动检查的架构规则（定义在 `ArchitectureTest.java`）：

| 级别 | 规则 | 说明 |
|------|------|------|
| P0 | 分层依赖 | Common → Core → Biz → Web，不可反向依赖 |
| P0 | 无循环依赖 | 包之间禁止循环引用 |
| P0 | DO 隔离 | `@Entity`/`@Table` 标注的 DO 类不能流入 Web 层 |
| P1 | Action 规范 | `@Action` 注解类必须实现 `BusinessAction` 接口 |
| P1 | Controller 命名 | 必须以 `Controller` 结尾，方法返回 `ResponseDTO` |
| P1 | DomainService 命名 | Core 层服务接口必须以 `DomainService` 结尾 |
| P2 | 日志规范 | 禁止 `System.out.println`，使用 Slf4j |

## API 设计规范

- 统一响应体：`ResponseDTO<T>`（code + msg + data）
- 错误码体系：200=成功，202=参数异常，208=系统异常，42xxx=资源不存在
- 全局异常处理：`@ControllerAdvice` → `GlobalExceptionHandler`
- GET 用查询参数，POST 用 JSON Body
- 枚举输出字符串值，不输出数字序号
