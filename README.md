# 股票交易策略回测分析系统

从腾讯股票公开接口获取日 K 线数据，应用多种交易策略（MACD、均线等）进行历史回测，生成统计指标和可视化报表。同时提供**元数据管理系统**，支持动态定义数据模型并生成 JSON Schema。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 2.5.6 (JDK 17) |
| 构建工具 | Maven 多模块 |
| 数据库 | MySQL 8.0 + MyBatis |
| 前端 | React 18 + Ant Design Pro (UmiJS 4) |
| 数据源 | 腾讯股票接口 (web.ifzq.gtimg.cn) |
| 架构测试 | ArchUnit (分层依赖守卫) |
| 集成测试 | Testcontainers + H2 |
| 测试框架 | JUnit 5 + Mockito |

## 快速开始

### 环境要求

- **JDK 17**
- **Node.js >= 12**
- **MySQL 8.0+**

```bash
# JDK 17 环境设置
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
```

### 后端启动

```bash
# 1. 初始化数据库
mysql -u root -p < basic_init.sql

# 2. 编译项目
mvn clean install -DskipTests

# 3. 启动后端（使用 test profile 连接本地 MySQL）
mvn spring-boot:run -pl stock-web -Dspring-boot.run.profiles=test
# 访问: http://localhost:8080
```

### 前端启动

```bash
cd stock-frontend
npm install
npm start
# 访问: http://localhost:8000
# API 自动代理至 http://localhost:8080
```

### 一体化快速启动

```bash
# 初始化 DB + 启动后端
mvn clean install -DskipTests && mvn spring-boot:run -pl stock-web -Dspring-boot.run.profiles=test

# 另一个终端启动前端
cd stock-frontend && npm install && npm start
```

## 项目架构

```
stock-web       (启动入口 + 架构测试)
  └─ stock-biz  (业务逻辑、流程引擎、Controller、交易策略)
       ├─ stock-core  (领域模型、Domain Service)
       │    └─ stock-common (MyBatis Mapper、DO、工具类)
       └─ stock-remote  (腾讯股票 API 客户端)
stock-integration-test  (跨模块集成测试)
```

> 详细架构说明见 [doc/architecture.md](doc/architecture.md)

## 核心功能

### 交易策略回测

| 端点 | 说明 |
|------|------|
| `GET /api/stock-strategy/list.json` | 策略列表 |
| `GET /api/stock-strategy/detail.json?name=xxx` | 策略详情（含指标） |
| `GET /api/stock-strategy/stock-detail.json?name=xxx&code=xxx` | 单股回测明细 |
| `POST /api/stock-strategy/create-regression.json?name=xxx` | 创建回归任务 |

### 元数据管理

支持动态定义数据模型、字段、枚举，自动生成 JSON Schema：

| 端点 | 说明 |
|------|------|
| `GET /api/metadata/model/list.json` | 模型列表 |
| `POST /api/metadata/model/save.json` | 新建/更新模型 |
| `GET /api/metadata/model/detail.json?id=xxx` | 模型详情（含字段） |
| `GET /api/metadata/model/schema.json?id=xxx` | 生成 JSON Schema |
| `POST /api/metadata/model/publish.json` | 发布模型 |
| `POST /api/metadata/model/delete.json?id=xxx` | 删除模型 |
| `POST /api/metadata/field/save.json` | 新建/更新字段 |
| `DELETE /api/metadata/field/delete.json?id=xxx` | 删除字段 |
| `GET /api/metadata/enum/list.json` | 枚举列表 |
| `POST /api/metadata/enum/save.json` | 新建/更新枚举 |
| `DELETE /api/metadata/enum/delete.json?id=xxx` | 删除枚举 |
| `GET /api/metadata/indicator/meaning?code=xxx` | 查询指标语义 |
| `GET /api/metadata/indicator/usage?code=xxx` | 查询指标用法 |

### 流程引擎

核心执行架构，将复杂业务拆分为原子 Action 的链式执行，通过 JSON 配置文件声明式编排：

```
Controller → ProcessContext(productCode, businessCode) → ProcessExecutor
              → 按 stage 执行 Action 链 → Router 流转下一阶段
```

> 详见 [doc/process-engine.md](doc/process-engine.md)

## 测试

```bash
# 单元测试
mvn test

# 集成测试（使用 Testcontainers）
mvn verify

# 仅运行架构测试
mvn test -pl stock-web -Dtest=ArchitectureTest
```

## 文档

- [架构设计](doc/architecture.md) — 设计哲学、分层架构、核心概念
- [模块详解](doc/modules.md) — 各模块完整包结构和设计决策
- [流程引擎](doc/process-engine.md) — Process Engine 设计原理和执行机制
- [数据流](doc/data-flow.md) — 核心数据流和业务流转

## 开发约定

- API 统一响应体 `ResponseDTO<T>`（code + msg + data）
- Controller 使用构造器注入（禁止 `@Resource` 字段注入）
- DbConverter 中所有 NOT NULL 列必须有 null-safe 默认值（[规则](.claude/rules/java/db-converter.md)）
- 分层依赖由 ArchUnit 守卫（禁止下层依赖上层）
- 提交遵循 Conventional Commits 格式
