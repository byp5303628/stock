# 元数据模型版本化与 JSONSchema 缓存 — 方案设计

## 1. 需求概述

### 业务目标

为元数据模型引入版本管理能力，支持发布历史追溯和版本切换，同时将 JSONSchema 生成结果缓存到数据库，消除每次请求时的运行时计算开销。

### 核心场景

1. **版本记录**：每次发布操作生成不可变的版本快照，历史版本永久保留。
2. **变更检测**：保存后自动判断当前工作区与最近发布版本是否一致，驱动状态机（DRAFT / CHANGING / PUBLISHED）。
3. **版本切换**：独立 API 指定当前生效版本，切换后所有查询使用该版本数据。
4. **Schema 缓存**：发布时计算 JSONSchema 并存储在版本表，查询时直接读缓存。

### 状态定义

| 状态 | 触发条件 |
|------|---------|
| DRAFT | 新建模型，从未发布过 |
| PUBLISHED | 发布后，当前工作区与最近发布版本 hash 一致 |
| CHANGING | 已发布过，但当前工作区与 snapshot_hash 不一致 |

---

## 2. 模型设计

### 2.1 数据库变更

#### 新增表：`metadata_model_version`

```sql
CREATE TABLE IF NOT EXISTS metadata_model_version
(
    id             BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    model_code     VARCHAR(64)  NOT NULL COMMENT '模型编码（冗余，便于按 code 查询）',
    model_id       BIGINT       NOT NULL COMMENT '所属模型 ID',
    version        INT          NOT NULL COMMENT '版本号，从 1 开始单调递增',
    schema_content MEDIUMTEXT            COMMENT 'JSONSchema 内容（JSON 字符串）',
    version_desc   VARCHAR(512) NOT NULL DEFAULT '' COMMENT '版本说明',
    gmt_create     DATETIME     NOT NULL DEFAULT NOW(),
    gmt_modified   DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    UNIQUE INDEX uniq_idx_model_version (model_id, version),
    INDEX idx_model_code (model_code),
    INDEX idx_gmt_create (gmt_create)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '元数据模型版本表';
```

#### 修改表：`metadata_model`（ALTER TABLE）

```sql
ALTER TABLE metadata_model
    ADD COLUMN current_version   INT          NOT NULL DEFAULT 0
        COMMENT '当前生效版本号（0 表示未切换，使用最新版）' AFTER status,
    ADD COLUMN snapshot_hash     VARCHAR(64)            DEFAULT NULL
        COMMENT '最近一次发布时的模型快照 hash（SHA-256 前 16 字节 hex）' AFTER current_version;
```

> `current_version = 0` 表示"未显式切换，始终使用最新版"；切换后为具体版本号。

#### DbConverter 默认值补充（遵循 db-converter.md 规范）

新增 `MetadataModelVersionDO.toDbEntity()` 时需对齐：
- `version_desc` → `""`
- `schema_content` → `null`（允许 null）

`MetadataModelDO` 新增字段默认值：
- `currentVersion` → `0`
- `snapshotHash` → `null`（可 null）

### 2.2 DO 类变更

#### 修改：`MetadataModelDO`

新增两个字段：

```java
// 在 stock-common/.../entity/MetadataModelDO.java
private Integer currentVersion;   // 当前生效版本
private String snapshotHash;      // 发布快照 hash
```

同步更新 `MetadataModelMapper.xml`：
- `BaseResultMap` 新增两列映射
- `insert` 语句新增两列（使用 DB DEFAULT 默认值，无需在 SQL 中写死）
- `updateById` 语句新增两列

#### 新增：`MetadataModelVersionDO`

```java
// stock-common/.../entity/MetadataModelVersionDO.java
@Getter
@Setter
public class MetadataModelVersionDO {
    private Long id;
    private String modelCode;
    private Long modelId;
    private Integer version;
    private String schemaContent;  // JSON 字符串
    private String versionDesc;
    private Date gmtCreate;
    private Date gmtModified;
}
```

### 2.3 领域模型变更

#### 修改：`MetadataModel`

```java
// stock-core/.../model/metadata/MetadataModel.java
private Integer currentVersion;   // 当前生效版本（0 = 最新）
private String snapshotHash;      // 发布快照 hash
```

#### 新增：`MetadataModelVersion`

```java
// stock-core/.../model/metadata/MetadataModelVersion.java
@Getter
@Setter
public class MetadataModelVersion {
    private Long id;
    private String modelCode;
    private Long modelId;
    private Integer version;
    private Map<String, Object> schemaContent;  // 反序列化后的 Schema
    private String versionDesc;
    private Date gmtCreate;
    private Date gmtModified;
}
```

### 2.4 状态模型

```
新建模型
  └─ 初始状态: DRAFT

首次发布
  └─ 写入版本 v1，计算 snapshotHash
  └─ 状态变更: DRAFT → PUBLISHED

发布后编辑（saveModel / saveField）
  └─ 重算当前 hash，与 snapshotHash 对比
  └─ 不一致: 状态变更: PUBLISHED → CHANGING

再次发布
  └─ 写入版本 v2，更新 snapshotHash
  └─ 状态变更: CHANGING → PUBLISHED

切换版本（switchVersion）
  └─ 仅更新 current_version 指针
  └─ 不修改工作区，不改变状态
```

---

## 3. 架构设计

### 3.1 模块划分

```
stock-common
  └─ entity/MetadataModelVersionDO.java        [新增]
  └─ mappers/MetadataModelVersionMapper.java   [新增]
  └─ resources/mappers/MetadataModelVersionMapper.xml  [新增]
  └─ entity/MetadataModelDO.java               [修改：+currentVersion, +snapshotHash]
  └─ resources/mappers/MetadataModelMapper.xml [修改：resultMap + insert + update]

stock-core
  └─ model/metadata/MetadataModelVersion.java  [新增]
  └─ model/metadata/MetadataModel.java         [修改：+currentVersion, +snapshotHash]
  └─ service/MetadataDomainService.java        [修改：新增 4 个方法]
  └─ service/impl/MetadataDomainServiceImpl.java [修改：publishModel 重写，新增方法]
  └─ converter/DbConverter.java                [修改：新增 toDbEntity(MetadataModelVersion)]
  └─ converter/DomainConverter.java            [修改：新增 toDomain(MetadataModelVersionDO)]

stock-biz
  └─ controller/MetadataController.java        [修改：新增 3 个端点]
  └─ dto/ModelVersionDTO.java                  [新增]
  └─ dto/ModelVersionListDTO.java              [新增]
  └─ dto/SwitchVersionRequest.java             [新增]
  └─ dto/PublishModelRequest.java              [新增]
  └─ converter/DtoConverter.java               [修改：新增 toDto(MetadataModelVersion)]

stock-frontend
  └─ services/metadata.js                      [修改：新增 3 个 API 函数]
  └─ pages/Metadata/ModelDetail/index.jsx      [修改：新增版本列表面板]
```

### 3.2 Mapper 设计

#### `MetadataModelVersionMapper`（新增）

```java
@Mapper
public interface MetadataModelVersionMapper {
    /** 插入新版本记录 */
    int insert(MetadataModelVersionDO versionDO);

    /** 按 modelId 查询全部版本，按 version DESC 排序 */
    List<MetadataModelVersionDO> selectByModelId(Long modelId);

    /** 按 modelId + version 查询单条 */
    MetadataModelVersionDO selectByModelIdAndVersion(Long modelId, Integer version);

    /** 查询最大版本号（用于生成下一版本） */
    Integer selectMaxVersionByModelId(Long modelId);
}
```

### 3.3 领域服务接口变更

```java
// MetadataDomainService.java — 新增 / 修改方法

/** 发布模型（重写原 publishModel，增加版本化逻辑） */
void publishModel(Long modelId, String versionDesc);

/** 查询版本列表 */
List<MetadataModelVersion> listModelVersions(Long modelId);

/** 切换当前生效版本 */
void switchModelVersion(Long modelId, Integer version);

/** 查询指定版本的 JSONSchema（优先读缓存） */
Map<String, Object> getSchemaByVersion(Long modelId, Integer version);

/** 计算当前模型工作区的 hash（供变更检测使用，可对外暴露用于测试） */
String computeModelHash(Long modelId);
```

> 原 `publishModel(Long modelId)` 签名变更为 `publishModel(Long modelId, String versionDesc)`。
> 原 `generateJsonSchema(Long modelId)` 保留，但内部调用时优先读 DB 缓存。

### 3.4 控制器 API 设计

#### 新增端点

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/metadata/model/versions.json?id={modelId}` | 查询版本列表 |
| `POST` | `/api/metadata/model/switch-version.json` | 切换当前版本 |
| `GET` | `/api/metadata/model/version-schema.json?id={modelId}&version={v}` | 查询指定版本 schema |

#### 修改端点

| 路径 | 变更说明 |
|------|---------|
| `POST /api/metadata/model/publish.json` | 请求体新增 `versionDesc` 字段 |

---

## 4. 关键逻辑

### 4.1 变更检测算法

**Hash 计算范围**

对以下字段做 SHA-256，取前 32 个 hex 字符（16 字节）作为 `snapshotHash`：

```
输入材料（排序后的 JSON 字符串）:
{
  "modelName": ...,
  "modelType": ...,
  "description": ...,
  "fields": [
    {
      "fieldName": ...,
      "fieldType": ...,
      "businessMeaning": ...,
      "required": ...,
      "constraints": ...,
      "enumId": ...,
      "sortOrder": ...
    }
    // 按 sortOrder ASC, fieldName ASC 排序
  ]
}
```

**不纳入 hash 的字段**：`status`、`extInfo`、`id`、`gmtCreate`、`gmtModified`、`currentVersion`、`snapshotHash`。

**实现位置**：`MetadataDomainServiceImpl` 中私有方法 `computeModelHash(Long modelId)`，调用链：
1. 查询 `metadata_model`（name/code/modelType/description）
2. 查询 `metadata_field`（按 sortOrder ASC, fieldName ASC 排序）
3. 序列化为 JSON，SHA-256，取前 32 hex 字符

**触发时机**：`saveModel()` 和 `saveField()` 成功写库后，立即调用 `refreshModelStatus(modelId)` 更新 `status`。

```java
// refreshModelStatus 内部逻辑（私有方法）
private void refreshModelStatus(Long modelId) {
    MetadataModelDO modelDO = metadataModelMapper.selectById(modelId);
    if (modelDO == null) return;
    
    // 从未发布过 → 保持 DRAFT
    if (modelDO.getSnapshotHash() == null) return;
    
    String currentHash = computeModelHash(modelId);
    String newStatus = currentHash.equals(modelDO.getSnapshotHash())
            ? "PUBLISHED" : "CHANGING";
    
    if (!newStatus.equals(modelDO.getStatus())) {
        modelDO.setStatus(newStatus);
        metadataModelMapper.updateById(modelDO);
        // 清 Cache
        Cache cache = cacheManager.getCache(CACHE_MODELS);
        if (cache != null) cache.clear();
    }
}
```

### 4.2 发布流程（Step by Step）

```
POST /api/metadata/model/publish.json  { modelId, versionDesc }
  │
  ├─ 1. 查询模型是否存在（不存在 → 抛 BusinessException）
  │
  ├─ 2. 计算下一版本号
  │      nextVersion = selectMaxVersionByModelId(modelId) ?? 0 + 1
  │
  ├─ 3. 生成 JSONSchema
  │      schemaMap = generateJsonSchema(modelId)  （复用现有逻辑）
  │      schemaContent = JSON.toJSONString(schemaMap)
  │
  ├─ 4. 写入版本表（在事务内）
  │      INSERT INTO metadata_model_version
  │        (model_id, model_code, version, schema_content, version_desc)
  │
  ├─ 5. 计算当前 hash
  │      currentHash = computeModelHash(modelId)
  │
  ├─ 6. 更新 metadata_model
  │      SET status = 'PUBLISHED',
  │          snapshot_hash = currentHash
  │      （注意：current_version 不改变）
  │
  ├─ 7. 清除模型 Cache（CACHE_MODELS）
  │
  └─ 8. 返回成功（void）
```

整个发布流程包裹在 `@Transactional` 内，步骤 4 和步骤 6 在同一事务中。

### 4.3 版本切换流程

```
POST /api/metadata/model/switch-version.json  { modelId, version }
  │
  ├─ 1. 验证版本记录存在（不存在 → BusinessException）
  │
  ├─ 2. UPDATE metadata_model SET current_version = #{version} WHERE id = #{modelId}
  │
  ├─ 3. 清 Cache
  │
  └─ 4. 返回成功
```

切换后，`getModelSchema(modelId)` 的行为：

```
getModelSchema(modelId):
  modelDO = selectById(modelId)
  effectiveVersion = modelDO.currentVersion == 0
      ? selectMaxVersionByModelId(modelId)   // 0 = 最新版
      : modelDO.currentVersion
  
  versionDO = selectByModelIdAndVersion(modelId, effectiveVersion)
  if versionDO != null && versionDO.schemaContent != null:
      return JSON.parseObject(versionDO.schemaContent)  // 命中缓存
  else:
      return generateJsonSchema(modelId)                  // fallback 实时计算
```

### 4.4 schema 查询降级策略

| 情况 | 行为 |
|------|------|
| 版本记录存在，schemaContent 有值 | 直接返回缓存内容 |
| 版本记录存在，schemaContent 为 null | fallback 实时计算 |
| 版本记录不存在（current_version 指向不存在版本） | fallback 实时计算 |
| 查询指定版本 schema（`getSchemaByVersion`） | 按版本精确读缓存，无 fallback（明确报错） |

---

## 5. DTO 设计

### 新增 DTO（`stock-biz/.../dto/`）

#### `ModelVersionDTO`

```java
@Getter
@Setter
public class ModelVersionDTO {
    private Long id;
    private Integer version;
    private String versionDesc;
    private Boolean isCurrent;  // 是否为 current_version 指向的版本
    private String gmtCreate;   // 发布时间，格式 yyyy-MM-dd HH:mm:ss
}
```

#### `SwitchVersionRequest`

```java
@Getter
@Setter
public class SwitchVersionRequest {
    @NotNull(message = "modelId 不能为空")
    private Long modelId;

    @NotNull(message = "version 不能为空")
    private Integer version;
}
```

#### `PublishModelRequest`（替换原 `ValidateRequest` 用于发布）

```java
@Getter
@Setter
public class PublishModelRequest {
    @NotNull(message = "modelId 不能为空")
    private Long modelId;

    @Size(max = 512, message = "版本说明不超过 512 字")
    private String versionDesc;  // 可选
}
```

### 修改 DTO

#### `MetadataModelDTO`（新增字段）

```java
private Integer currentVersion;           // 当前生效版本（0 表示最新）
private List<ModelVersionDTO> versions;   // 版本列表（详情页查询时填充）
```

---

## 6. 前端影响

### 6.1 `src/services/metadata.js`（新增 3 个函数）

```javascript
export async function listModelVersions(modelId) {
  return request(`/api/metadata/model/versions.json?id=${modelId}`);
}

export async function switchModelVersion(data) {
  return request('/api/metadata/model/switch-version.json', { method: 'POST', data });
}

export async function getVersionSchema(modelId, version) {
  return request(`/api/metadata/model/version-schema.json?id=${modelId}&version=${version}`);
}
```

**修改**：原 `publishModel` 请求体新增 `versionDesc` 可选字段（前端传空字符串或省略均可，无破坏性变更）。

### 6.2 `ModelDetail/index.jsx`

在现有 "发布" 按钮下方新增 "版本历史" 面板：

**新增状态**

```javascript
const [versions, setVersions] = useState([]);
const [versionsVisible, setVersionsVisible] = useState(false);
```

**新增方法**

```javascript
const handleLoadVersions = () => {
  listModelVersions(Number(modelId)).then(r => {
    if (r && r.code === 200) {
      setVersions(r.data || []);
      setVersionsVisible(true);
    }
  });
};

const handleSwitchVersion = (version) => {
  switchModelVersion({ modelId: Number(modelId), version }).then(r => {
    if (r && r.code === 200) {
      message.success(`已切换到版本 v${version}`);
      refreshModel();
    }
  });
};
```

**新增 UI 元素**

1. 在操作按钮区新增 "版本历史" 按钮（点击调用 `handleLoadVersions`）。
2. 抽屉（`Drawer`）或折叠面板展示版本列表，列包含：版本号、发布时间、版本说明、是否当前版、操作（切换）。
3. 版本列表按版本号 DESC 展示。
4. 状态标签新增 `CHANGING` → `{ color: 'orange', text: '变更中' }`（`STATUS_MAP` 已有 DEPRECATED，参照同样方式新增）。

> 注意：`STATUS_MAP` 现有 `DRAFT` / `PUBLISHED` / `DEPRECATED`，缺少 `CHANGING`，需补充。

### 6.3 `ModelList/index.jsx`

无需修改（列表页不展示版本信息）。

---

## 7. 影响面分析

### 7.1 需要修改的现有代码

| 文件 | 变更类型 | 说明 |
|------|---------|------|
| `basic_init.sql` | 新增 DDL + ALTER | 新增 `metadata_model_version` 表；修改 `metadata_model` 表 |
| `MetadataModelDO` | 字段新增 | `currentVersion`, `snapshotHash` |
| `MetadataModelMapper.xml` | XML 修改 | resultMap/insert/update 新增两列 |
| `MetadataDomainService` | 接口修改 | `publishModel` 签名变更（新增 `versionDesc` 参数）；新增 4 个方法 |
| `MetadataDomainServiceImpl` | 实现修改 | `publishModel` 完全重写；新增 5 个私有/公共方法 |
| `DbConverter` | 新增方法 | `toDbEntity(MetadataModelVersion)` |
| `DomainConverter` | 新增方法 | `toDomain(MetadataModelVersionDO)` |
| `MetadataController` | 新增端点 | 3 个新端点；`publishModel` 参数类型改为 `PublishModelRequest` |
| `DtoConverter` | 新增方法 | `toDto(MetadataModelVersion)` |
| `MetadataModelDTO` | 字段新增 | `currentVersion`, `versions` |
| `metadata.js`（前端） | 新增函数 | 3 个 API 调用函数 |
| `ModelDetail/index.jsx`（前端） | 功能扩展 | 版本历史面板、状态标签补全 |

### 7.2 兼容性影响

- **`publishModel` 签名变更**：原接口 `publishModel(Long modelId)` 变为 `publishModel(Long modelId, String versionDesc)`。Controller 层改用 `PublishModelRequest` 替代 `ValidateRequest`，需同步修改 Controller 的方法参数类型，但 API 路径不变（`/api/metadata/model/publish.json`）。前端 `publishModel` 调用时新增可选字段，无破坏性变更。
- **`getModelSchema` 行为变化**：接口路径和响应格式不变，但内部会优先读 DB 缓存。对于从未发布的模型（无版本记录），fallback 为原有实时计算逻辑，行为等价。
- **`saveModel` / `saveField` 副作用新增**：写操作成功后会调用 `refreshModelStatus`，额外多一次 DB 查询（读当前字段）和可能的 UPDATE。对于高频写场景需注意，但正常使用下无性能问题。
- **数据库 ALTER**：`metadata_model` 新增两列均有 DEFAULT，不影响现有数据行（旧行 `current_version=0, snapshot_hash=NULL`）。

### 7.3 数据迁移

- 存量模型的 `current_version = 0`（DB DEFAULT），`snapshot_hash = NULL`。
- 存量已发布模型状态保持不变（`PUBLISHED`），等待下次发布时生成第一个版本记录并写入 snapshotHash。
- 存量草稿模型状态保持 `DRAFT`，行为不变。
- 无需数据补全脚本（新字段的含义允许从 0/NULL 起步）。

### 7.4 回滚方案

- 回滚 Java 代码至发布前版本。
- 执行 `ALTER TABLE metadata_model DROP COLUMN current_version, DROP COLUMN snapshot_hash`。
- 执行 `DROP TABLE IF EXISTS metadata_model_version`。
- 存量状态如有 `CHANGING`，回滚后数据库中仍为 `CHANGING`，但旧代码不认识该状态（已有 `STATUS_MAP` 包含 `CHANGING` 兜底渲染逻辑，或改回 PUBLISHED 即可）。

---

## 6. 未解决问题

1. **`versionDesc` 是否必填**：当前设计为可选（`@Size(max=512)`，可为空字符串）。若产品要求必填，需在 `PublishModelRequest` 上改为 `@NotBlank`，并在前端弹出填写弹框。

2. **`current_version = 0` 语义歧义**：`0` 表示"未显式切换，使用最新版"，但 `0` 也是一个合法的自增 ID 起始边缘值。建议将 `version` 从 `1` 开始分配（已在 DDL 和逻辑中约定），保证 `current_version = 0` 始终等价于"最新版"语义，不会与实际版本号冲突。

3. **并发发布**：当前发布流程未对同一 modelId 加锁，若并发两次发布，`selectMaxVersionByModelId` 可能读到相同的 maxVersion 导致版本号冲突。`(model_id, version)` 唯一索引会兜底拦截，但会抛出 `DuplicateKeyException`。如需精确处理，可在 Service 层使用 `SELECT ... FOR UPDATE` 或数据库乐观锁。当前 PRD 未提及并发发布场景，暂不处理，由唯一索引兜底。

4. **版本号上限**：使用 `INT`（最大 ~21 亿），正常业务场景下不会溢出，无需处理。

5. **旧版本 SchemaContent 补全**：发布时才生成 SchemaContent，历史发布前的数据无缓存。若需要补全历史数据，需要一次性脚本，但当前需求未要求，跳过。

6. **Cache 策略与版本切换一致性**：`@Cacheable(value = CACHE_MODELS, key = "'all'")` 缓存了全量模型列表，其中不包含 `versions` 字段。版本相关查询（`listModelVersions`）不走缓存，直接查 DB，因此版本数据始终最新，无一致性问题。
