# 建设通用的指标体系 — 任务拆分

## 阶段一：基础准备（预估：3.5 人天）

### T-1 创建数据库表

- **描述**: 新建 `indicator_definition` 和 `indicator_value`（含分区）两张表
- **涉及文件**: `basic_init.sql`（追加 DDL）
- **依赖**: 无
- **验收条件**:
  - [ ] `indicator_definition` 表创建成功，唯一索引生效
  - [ ] `indicator_value` 分区表创建成功，季度分区策略正确
  - [ ] 插入重复数据时触发唯一索引冲突
- **预估**: 0.5 人天

### T-2 创建 DO 实体与 MyBatis Mapper

- **描述**: 创建 `IndicatorDefinitionDO`、`IndicatorValueDO` 实体类及 Mapper 接口/XML
- **涉及文件**: `stock-common/.../entity/`, `stock-common/.../mappers/`
- **依赖**: T-1
- **验收条件**:
  - [ ] DO 字段与数据库字段一一对应
  - [ ] Mapper 支持 CRUD + 按 code+entity+date 精准查询 + 时间范围查询
- **预估**: 1 人天

### T-3 创建领域模型与枚举

- **描述**: 创建 `IndicatorDefinition`、`IndicatorValue`、`AggregateResult`、`EntityType`、`IndicatorCategory`
- **涉及文件**: `stock-core/.../model/indicator/`
- **依赖**: 无
- **验收条件**: 领域模型与 DO 可互相转换，枚举定义完整
- **预估**: 0.5 人天

### T-4 创建 Converter

- **描述**: 实现 DO ↔ Domain ↔ DTO 转换
- **涉及文件**: `stock-core/.../converter/IndicatorConverter.java`
- **依赖**: T-2, T-3
- **验收条件**: 三层转换正确，枚举映射正确
- **预估**: 0.5 人天

### T-5 分区表管理脚本

- **描述**: 编写自动化分区管理 SQL 脚本（定期新建分区、归档历史分区）
- **涉及文件**: `basic_init.sql`、运维脚本
- **依赖**: T-1
- **验收条件**: 脚本可自动创建下个季度分区，可归档 3 年前历史数据
- **预估**: 1 人天

## 阶段二：核心功能（预估：5 人天）

### T-6 实现 IndicatorDomainService

- **描述**: 实现单点查询、范围查询、聚合查询、upsert 写入
- **涉及文件**: `stock-core/.../service/IndicatorDomainService`
- **依赖**: T-2, T-3, T-4
- **验收条件**:
  - [ ] 精准查询返回正确值
  - [ ] 时间范围查询返回完整列表
  - [ ] 聚合查询（AVG）正确
  - [ ] upsert 写入幂等
- **预估**: 2 人天

### T-7 实现指标定义管理 Controller

- **描述**: 提供指标定义增删改查接口
- **涉及文件**: `stock-biz/.../controller/IndicatorDefinitionController.java`
- **依赖**: T-6
- **验收条件**: list + save 接口符合 `ResponseDTO` 规范
- **预估**: 1 人天

### T-8 实现指标值查询 Controller

- **描述**: 提供单点查询、范围查询、聚合查询接口
- **涉及文件**: `stock-biz/.../controller/IndicatorValueController.java`
- **依赖**: T-6
- **验收条件**: get/list/aggregate 三个接口正确，参数校验完备
- **预估**: 1.5 人天

### T-9 实现指标值写入 Controller

- **描述**: 提供单条和批量写入接口
- **涉及文件**: `IndicatorValueController.java`
- **依赖**: T-8
- **验收条件**: 写入幂等，不存在指标编码时报错
- **预估**: 0.5 人天

## 阶段三：完善优化（预估：3 人天）

### T-10 接入缓存层

- **描述**: Caffeine L1 + Redis L2 缓存，含降级策略
- **涉及文件**: `stock-core/.../config/IndicatorCacheConfig.java`
- **依赖**: T-6
- **验收条件**: 缓存命中/失效正确，Redis 不可用时自动降级 DB
- **预估**: 1.5 人天

### T-11 集成测试

- **描述**: 覆盖所有 API 端点的正向和异常场景
- **涉及文件**: `stock-web/.../IndicatorIntegrationTest.java`
- **依赖**: T-7, T-8, T-9
- **验收条件**: `mvn test` 全部通过
- **预估**: 1 人天

### T-12 补充文档与配置

- **描述**: API 文档、配置说明、分区运维文档
- **依赖**: T-7, T-8, T-9
- **验收条件**: 所有配置项有注释，API 有请求/响应示例
- **预估**: 0.5 人天

## 工作量汇总

| 阶段 | 人天 |
|------|------|
| 基础准备 | 3.5 天 |
| 核心功能 | 5 天 |
| 完善优化 | 3 天 |
| **总计** | **11.5 天** |
