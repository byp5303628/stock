# 流程引擎设计

## 概述

Process Engine 是本项目的核心架构模式，将复杂业务流程分解为原子 Action 的链式执行，由 JSON 配置文件声明式编排。类似"状态机 + 责任链"的组合模式。

## 核心类

```
┌─────────────────────────────────────────────────────┐
│                  ProcessContext                      │
│  productCode, businessCode, stage, entity,          │
│  resultCode, resultMsg, processConfig               │
└─────────────────┬───────────────────────────────────┘
                  │ 被传递
┌─────────────────▼───────────────────────────────────┐
│                ProcessExecutor                       │
│  递归执行: stage 内 actions → router.route()        │
│  → 递归 execute() 下一 stage                        │
└─────────────────┬───────────────────────────────────┘
                  │ 按 stage 获取 actions
┌─────────────────▼───────────────────────────────────┐
│                ProcessConfig                         │
│  getActions(stage) → List<String>                   │
│  getRouter(stage)  → String (router bean 名)        │
└─────────────────┬───────────────────────────────────┘
                  │ 从 JSON 文件加载
┌─────────────────▼───────────────────────────────────┐
│           resources/process/*.json                   │
│  { processStageMap: { PROCESS: { actions: [...] } }} │
└─────────────────────────────────────────────────────┘
```

## 执行流程

```
1. Controller 创建 ProcessContext，设置 productCode + businessCode + entity
2. 从 ProcessConfigCache 获取 ProcessConfig（JSON 反序列化）
3. 调用 ProcessExecutor.execute(context)
4. ProcessExecutor 从 ProcessConfig 获取当前 stage 的 actions 列表
5. 从 Spring 容器获取对应的 Action Bean（按名称），逐个执行
6. 当前 stage 所有 action 执行完毕后，调用 Router.route() 决定下一 stage
7. 递归执行直到 stage == "FINISH"
```

## Stage 流转

```
PRE_PROCESS → PROCESS → POST_PROCESS → FINISH
                  ↓
             (遇到 ProcessException)
                  ↓
            ERROR_PROCESS → FINISH
```

默认 Router 按 `PRE_PROCESS → PROCESS → POST_PROCESS → FINISH` 顺序流转。

## Action

Action 是业务逻辑的最小执行单元：

```java
@Action("getTradePolicyByNameAction")  // Bean 名称，与 JSON 中 actions 数组对应
public class GetTradePolicyByNameAction implements BusinessAction {
    @Resource
    private TradePolicyFactory tradePolicyFactory;

    @Override
    public void process(ProcessContext context) {
        TradePolicyAware entity = context.getEntity();  // 从上下文获取实体
        String name = entity.getTradePolicyName();       // 从实体获取入参
        TradePolicy tradePolicy = tradePolicyFactory.getTradePolicy(name);
        entity.setTradePolicy(tradePolicy);              // 结果写回实体
    }
}
```

**设计要点**：
- Action 通过泛型方法 `context.getEntity()` 获取实体（不绑定具体类型）
- 通过 Aware 接口约束实体的能力（如 `TradePolicyAware` 声明实体需要/提供策略名称）
- Action 之间通过 Entity 传递数据（前一个 Action 写入，后一个 Action 读取）
- 遇到业务错误抛出 `ProcessException`，引擎自动转入 `ERROR_PROCESS` 阶段

## Entity 设计

Entity 是流程中的数据载体，通过接口定义契约：

```java
// Aware 接口：声明 Entity 需要什么数据 / 能提供什么数据
public interface TradePolicyAware {
    String getTradePolicyName();    // 输入：策略名称
    void setTradePolicy(TradePolicy tradePolicy);  // 输出：策略对象
}

public interface StockCntAware {
    void setStockCnt(Integer cnt);  // 输出：股票数量
}

// Entity 实现需要的 Aware 接口
public class StrategyDetailEntity extends BaseEntity
    implements TradePolicyAware, StockCntAware, StockRegressionDetailsAware {
    private String name;
    private Integer stockCnt;
    private TradePolicy tradePolicy;
    private List<StockRegressionDetail> stockRegressionDetails;
    private StrategyDetailDTO strategyDetailDTO;  // 最终结果
}
```

**设计动机**：
- Entity 不是贫血模型（只有 getter/setter），而是一个"过程文档"，记录流程中产出的中间结果
- Aware 接口是不同 Action 之间的隐式契约
- `BaseEntity.isSuccess()` 标记整个流程是否成功

## JSON 配置示例

```json
{
  "processStageMap": {
    "PROCESS": {
      "actions": [
        "getTradePolicyByNameAction",
        "getStockCntAction",
        "getPolicyRegressionDetailAction",
        "calculatePolicyDetailAction",
        "buildStrategyIndicatorRespAction"
      ]
    },
    "ERROR_PROCESS": {
      "actions": ["commonErrorHandleAction"]
    }
  }
}
```

文件命名规则：`{productCode}_{businessCode}.json`

## ProcessConfigCache

启动时扫描 `resources/process/*.json`，将 JSON 反序列化为 `ProcessConfigImpl`，按文件名（key）缓存到 Map。

```java
// 加载逻辑
String key = fileName.substring(0, fileName.indexOf(".json"));  // stock_strategy_detail
ProcessConfig config = JSON.parseObject(content, ProcessConfigImpl.class);
tempMap.put(key, config);

// 查询逻辑
String key = productCode + "_" + businessCode;  // stock_strategy_detail
return processConfigMap.get(key);
```

## 现有流程

| productCode | businessCode | 用途 |
|-------------|-------------|------|
| stock_strategy | detail | 获取策略详情（含指标） |
| stock_strategy | code_detail | 获取单只股票策略回测明细 |
| stock_strategy | create_regression | 创建回归任务 |

## 与传统 Service 模式对比

| 维度 | 传统 Service | 流程引擎 |
|------|-------------|---------|
| 业务逻辑组织 | 一个 Service 方法写所有逻辑 | 拆分为多个 Action + JSON 编排 |
| 代码复用 | Action 不能在多个流程间复用 | Action 是原子 Bean，多流程可共用 |
| 流程变更 | 改 Java 代码 | 改 JSON 配置（或新增 Action） |
| 可测试性 | 测试需要 mock 整个 Service | 每个 Action 独立测试 |
| 学习成本 | 低 | 需理解引擎机制 |
