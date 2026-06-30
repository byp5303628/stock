# 元数据模型版本化与 JSONSchema 缓存

## 背景与目标

当前元数据模型仅有草稿/发布两种状态，发布后模型即生效，不支持版本回滚或历史切换。同时 JSONSchema 的生成依赖实时计算（`generateJsonSchema` 方法），每次查询都需要遍历字段配置重新计算，缺少缓存机制。随着模型数量和字段复杂度的增长，实时计算的性能会成为瓶颈。

目标：
- 引入模型版本机制，每次发布生成一个新版本，版本切换由前端显式触发
- 将 JSONSchema 缓存到 DB 中，查询时直接从 DB 读取而非实时计算
- 前端支持版本列表展示和版本切换交互

## 需求范围

### In Scope

1. **模型版本化**
   - `metadata_model` 表增加 `current_version` 字段，记录当前生效的版本号
   - 新增 `metadata_model_version` 表，存储每次发布生成的版本记录
   - 版本记录包含：版本号（int，自增）、关联模型 ID、JSONSchema 内容

2. **模型状态与变更检测**
   - 模型的状态由"当前工作区"是否发生变更决定
   - 如果当前模型的属性或字段相比上次发布发生过变更，状态标记为"变更中"（CHANGING）
   - 如果当前工作区与已发布版本一致，状态标记为"已发布"（PUBLISHED）
   - 新增但从未发布的模型，标记为"草稿"（DRAFT）

3. **发布语义变更**
   - 当前：发布 = 将模型状态改为 PUBLISHED
   - 变更后：发布 = 基于当前工作区的属性与字段生成一个新版本（版本号 +1），同时计算 JSONSchema 写入版本记录，然后将模型的变更状态重置为"已发布"
   - 前端点击"切换版本"时，模型才会实际切换到所选版本

4. **JSONSchema 缓存**
   - 发布时自动计算 JSONSchema 并写入 `metadata_model_version` 表的 `schema_content` 字段
   - 查询 JSONSchema 时从 DB 读取对应版本的缓存，无需实时计算
   - 新增 `GET /api/metadata/model/schema.json?id={modelId}&version={version}` 接口，version 不传则取当前生效版本

5. **前端版本切换**
   - 模型详情页增加版本列表展示（版本号、发布时间、当前版本标记）
   - 提供"切换到此版本"按钮，点击后模型的当前版本指向所选版本
   - 版本列表按版本号降序排列，最新在前

### Out of Scope

- JSONSchema 的增量 diff / 版本对比
- 版本回滚的自动备份（手动切换即可回滚）
- 多环境版本同步（开发/测试/生产）

## 业务规则

### 模型状态与变更检测

| 状态 | 含义 | 触发条件 |
|------|------|---------|
| DRAFT | 草稿 | 模型新建后从未发布过 |
| CHANGING | 变更中 | 模型已有发布版本，但当前属性或字段发生过变更 |
| PUBLISHED | 已发布 | 当前工作区与最新发布的版本完全一致 |

变更检测规则：
- 发布成功后，记录当前模型属性（name、description、modelType 等）和字段列表（fieldName、fieldType、sortOrder 等）的哈希值
- 每次保存属性或字段时，重新计算哈希值与发布时的快照对比
- 哈希值不一致 → 状态标记为 CHANGING
- 哈希值一致 → 状态标记为 PUBLISHED（前提是至少发布过一次）
- 变更检测仅比较"当前是否与最新发布版本一致"，不跟踪具体变更历史

### 版本号管理

- 如果模型从未发布过，`current_version` 为 null
- 如果模型已有版本，每次发布版本号 +1（从 1 开始）
- 版本号单调递增，不重用已删除的版本号
- 全部历史版本永久保留，不做数量限制

### 发布规则

- 如果模型处于 DRAFT 状态且从未发布：发布时生成 version=1
- 如果模型已有版本：发布时生成 version = 当前最大版本号 + 1
- 发布时自动执行：
  1. 校验模型 Schema（同现有 validate 逻辑）
  2. 校验通过后，基于当前属性与字段生成 JSONSchema
  3. 在 `metadata_model_version` 表插入新版本记录（含 schema_content）
  4. 将模型状态重置为 PUBLISHED
  5. 记录发布时"属性与字段快照哈希"用于后续变更检测
- 发布失败（校验不通过）时，不生成版本，模型状态不变

### 版本切换规则

- 用户点击"切换到此版本"后，将模型的 `current_version` 更新为所选版本号
- 版本切换成功后，模型相关的查询接口（JSONSchema、字段详情等）以切换后的版本为准
- 版本切换仅改变`current_version`，不修改模型的当前工作区（属性、字段列表不受影响）
- 版本切换记录可追溯（在版本表中记录切换时间）

### JSONSchema 缓存

- 发布时自动调用 `generateJsonSchema` 计算 schema 并写入 `schema_content`（TEXT 类型）
- 查询 JSONSchema 时优先查 DB 缓存，缓存不存在时回退到实时计算
- 如果模型的字段配置在发布后被修改（未再次发布），schema 不更新，需要重新发布才能刷新缓存

## 数据库变更

### metadata_model 表变更

```sql
ALTER TABLE metadata_model
    ADD COLUMN current_version INT DEFAULT NULL COMMENT '当前生效版本号',
    ADD COLUMN published_snapshot VARCHAR(64) DEFAULT NULL COMMENT '上次发布时的字段快照哈希值';
```

### 新增 metadata_model_version 表

```sql
CREATE TABLE IF NOT EXISTS metadata_model_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_code VARCHAR(64) NOT NULL COMMENT '关联模型的 code（语义主键）',
    version INT NOT NULL COMMENT '版本号',
    schema_content TEXT COMMENT '发布时的 JSON Schema 内容',
    version_desc VARCHAR(512) DEFAULT '' COMMENT '版本说明',
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_model_code_version UNIQUE (model_code, version)
);
```

## 后端 API 变更

| 方法 | 端点 | 说明 | 变更类型 |
|------|------|------|---------|
| GET | `/api/metadata/model/schema.json?id={id}&version={version}` | 查询 JSONSchema，version 可选，不传则取当前生效版本 | 新增 |
| POST | `/api/metadata/model/publish.json` | 发布模型（校验 → 生成新版本 → 写入 schema 缓存） | 已有，增强 |
| POST | `/api/metadata/model/switch-version.json` | 切换当前生效版本 | 新增 |
| GET | `/api/metadata/model/versions.json?id={id}` | 获取模型版本列表 | 新增 |
| GET | `/api/metadata/model/detail.json?id={id}&version={version}` | 查询指定版本详情（含字段 + schema） | 已有，增强 |

## 验收条件

### AC1: 首次发布生成版本
- Given: 一个新增的模型，状态为 DRAFT，从未发布过
- When: 点击"发布"
- Then: 生成 version=1，JSONSchema 写入 schema_content，模型状态变为 PUBLISHED

### AC2: 属性变更后触发变更中状态
- Given: 模型已发布，状态为 PUBLISHED
- When: 添加、删除或修改任意属性或字段
- Then: 模型状态变为 CHANGING

### AC3: 再次发布版本递增
- Given: 模型状态为 CHANGING，已有 version=1
- When: 点击"发布"
- Then: 生成 version=2，版本号递增，模型状态重置为 PUBLISHED

### AC4: 版本切换
- Given: 模型有 version=1 和 version=2，当前生效 version=2
- When: 选择 version=1 点击"切换到此版本"
- Then: 模型的 current_version 变为 1，查询接口返回 version=1 的数据

### AC5: JSONSchema 查询
- Given: 模型已发布 version=2
- When: 查询 schema.json?version=2
- Then: 返回 DB 缓存的 JSONSchema，无需实时计算

### AC6: 发布不改变当前工作区
- Given: 模型当前工作区有未发布的字段变更
- When: 发布生成新版本
- Then: 当前工作区的属性和字段不受影响（保持发布前的状态）

### AC7: 前端版本列表
- Given: 模型有多个版本
- When: 打开模型详情页
- Then: 展示版本列表，按版本号降序排列，当前生效版本高亮，状态 OK 标签显示 PUBLISHED/CHANGING/DRAFT

## 非功能性要求

- JSONSchema 查询响应时间 < 50ms（DB 缓存，无需计算）
- 版本列表查询响应时间 < 100ms
- 版本表字段 `schema_content` 支持 TEXT 类型，足以存储中大型模型的 JSONSchema
- 全部历史版本永久保留，无数量限制
