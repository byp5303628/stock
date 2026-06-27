---
paths:
  - "stock-frontend/src/**/*.js"
  - "stock-frontend/src/**/*.jsx"
  - "stock-frontend/src/**/*.ts"
  - "stock-frontend/src/**/*.tsx"
  - "stock-frontend/config/**/*.js"
  - "stock-frontend/config/**/*.ts"
description: 前端项目规范 — Ant Design Pro 目录结构 + Controller-Service 1:1 映射
---

# 前端项目规范

> 项目级前端约束，覆盖 Ant Design Pro 目录结构和后端 Controller 一对一映射规则。

## Ant Design Pro 目录规范

### 强制目录结构

```
stock-frontend/src/
  ├─ pages/              # 页面组件（按路由组织）
  │   ├─ Welcome/        # 每个页面一个目录，PascalCase
  │   ├─ StrategyList/
  │   └─ Metadata/
  │       ├─ ModelList/
  │       ├─ ModelDetail/
  │       └─ EnumManager/
  │
  ├─ services/           # API 服务层（每个后端 Controller 对应一个文件）
  │   ├─ strategy.js
  │   ├─ metadata.js
  │   └─ schedule-config.js
  │
  ├─ components/         # 公共组件（跨页面复用）
  │   ├─ Footer/
  │   ├─ HeaderDropdown/
  │   └─ RightContent/
  │
  ├─ locales/            # 国际化
  │   └─ zh-CN/
  │
  └─ utils/              # 工具函数
```

### 页面目录规范（R1）

每个页面必须是一个独立目录，入口文件为 `index.jsx`：

```
pages/SomePage/
  └─ index.jsx          # 页面入口（唯一入口点）
```

**禁止**直接在 `pages/` 下放单文件页面：

```
# ❌ 错误
pages/SomePage.jsx

# ✅ 正确
pages/SomePage/index.jsx
```

### 嵌套页面（R2）

子页面放在父页面目录下，路由嵌套对应目录嵌套：

```
pages/StrategyDetail/
  ├─ index.jsx                    # /strategy/strategy-list/strategy-detail
  └─ StockStrategyDetail/
       └─ index.jsx               # /strategy/strategy-list/strategy-detail/stock-strategy-detail
```

### Service 文件命名（R3）

Service 文件使用 **camelCase 单文件**，不放在子目录中：

```
services/
  ├─ strategy.js         # ✅ camelCase
  ├─ schedule-config.js  # ❌ kebab-case，应改为 scheduleConfig.js
  └─ metadata.js         # ✅
```

> 已存在的 kebab-case 命名可保留，新增 service 必须使用 camelCase。

## Controller-Service 1:1 映射（强制）

### 核心规则

**每个后端 Controller 必须对应且仅对应一个前端 service 文件，反之亦然。**

```java
// 后端: StockStrategyController.java
@RestController
@RequestMapping("/api/stock-strategy")
public class StockStrategyController { ... }
```

```javascript
// 前端: src/services/strategy.js
// 仅包含 /api/stock-strategy/* 的请求函数
export async function getStockStrategyList() {
  return request("/api/stock-strategy/list.json");
}
```

### 映射对照表（R4）

新增接口时，必须遵循此表。若找不到对应的 Controller/Service，说明架构有问题。

| 后端 Controller | URL 前缀 | 前端 Service | 说明 |
|-----------------|----------|-------------|------|
| `StockStrategyController` | `/api/stock-strategy` | `services/strategy.js` | 策略查询与回归 |
| `MetadataController` | `/api/metadata` | `services/metadata.js` | 元数据 CRUD（model/field/enum） |
| `MetadataIntegrationController` | `/api/metadata/indicator` | `services/indicator.js` | 指标语义与用法查询 |
| `ScheduleConfigController` | `/api/schedule-config` | `services/scheduleConfig.js` | 调度配置管理 |
| `StockBasicListController` | `/api/stock-basic` | — | 空 Controller，暂不映射 |

### 违规检测（R5）

以下模式视为违规：

```
# ❌ 一个 service 包含多个 Controller 的接口
services/metadata.js:
  export getModelDetail()     # /api/metadata/model → MetadataController ✅
  export getIndicatorMeaning() # /api/metadata/indicator → MetadataIntegrationController ❌ 串了！

# ❌ 多个 service 调用同一个 Controller
services/strategy-list.js  → GET /api/stock-strategy/list.json     ❌
services/strategy-detail.js → GET /api/stock-strategy/detail.json  ❌

# ✅ 一个 Controller 的所有端点在一个 service
services/strategy.js → 所有 /api/stock-strategy/* 端点 ✅
```

### 新增 Controller 时的操作（R6）

1. 后端新增 `XxxController.java`
2. 前端在 `services/` 下创建 `xxxService.js`（camelCase）
3. 将 Controller 的 URL 前缀作为 service 文件的基础路径
4. 更新本规则文件中的映射对照表

### 新增 API 端点时的操作（R7）

1. 确认所属 Controller
2. 在对应的 service 文件中新增函数
3. **不要**在别的 service 中跨 Controller 添加接口

## 检查清单

提交包含前后端接口变动的代码前：

- [ ] 每个后端 Controller 只对应一个前端 service 文件
- [ ] 每个前端 service 文件只对应一个后端 Controller
- [ ] 新增页面放在独立目录中（`pages/XxxPage/index.jsx`）
- [ ] 新增 service 文件使用 camelCase 命名
- [ ] 已更新本规则的映射对照表
- [ ] `schedule-config.js` → `scheduleConfig.js`（待迁移）
