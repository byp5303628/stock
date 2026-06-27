# 实现方案: 元数据模型版本化与 JSONSchema 缓存

## 概述

本方案为元数据管理引入版本控制和 JSONSchema 缓存机制。每次发布生成不可变的版本快照（含缓存的 JSONSchema），支持版本列表查看和版本切换。引入变更检测机制，当模型属性或字段被修改后自动将状态标记为 CHANGING。

## 需求

- 新增 `metadata_model_version` 表存储每次发布生成的版本记录（含 schema_content 缓存）
- `metadata_model` 表增加 `current_version` 和 `snapshot_hash` 字段
- 变更检测: saveModel / saveField 后自动计算 hash 对比，驱动 DRAFT / CHANGING / PUBLISHED 状态机
- 发布流程重写: 校验后生成版本号 +1 的新版本，计算 JSONSchema 写入版本表
- 版本切换: 独立 API 仅更新 current_version 指针，不影响工作区
- JSONSchema 查询优先读 DB 缓存，无缓存时 fallback 实时计算
- 前端 ModelDetail 页面新增版本历史面板和版本切换按钮

## 架构变更

- `stock-common`:
  - 新增 `MetadataModelVersionDO` 实体
  - 新增 `MetadataModelVersionMapper` 接口 + XML
  - 修改 `MetadataModelDO`，新增 `currentVersion`、`snapshotHash` 字段
  - 修改 `MetadataModelMapper.xml`，新增两列映射
- `stock-core`:
  - 新增 `MetadataModelVersion` 领域模型
  - 修改 `MetadataModel` 领域模型，新增 `currentVersion`、`snapshotHash` 字段
  - 修改 `MetadataDomainService` 接口，publishModel 签名变更，新增 4 个方法
  - 修改 `MetadataDomainServiceImpl`，重写 publishModel，新增变更检测/版本查询/切换逻辑
  - 修改 `DbConverter`，新增 `toDbEntity(MetadataModelVersion)`
  - 修改 `DomainConverter`，新增 `toDomain(MetadataModelVersionDO)`
- `stock-biz`:
  - 新增 `ModelVersionDTO`、`SwitchVersionRequest`、`PublishModelRequest`
  - 修改 `MetadataModelDTO`，新增 `currentVersion`、`versions` 字段
  - 修改 `DtoConverter`，新增 `toDto(MetadataModelVersion)`
  - 修改 `MetadataController`，新增 3 个端点，修改 publishModel 签名
  - 修改 `ErrorCode`，新增 `METADATA_VERSION_NOT_FOUND` (42006)
- `stock-integration-test`:
  - 修改 `init-schema.sql`，新增版本表 + ALTER TABLE
  - 修改 `MetadataIT`，新增版本化验收测试
- `stock-frontend`:
  - 修改 `services/metadata.js`，新增 3 个 API 函数
  - 修改 `ModelDetail/index.jsx`，新增版本历史面板和状态标签

## 实现步骤

### 阶段1: 数据库 + Mapper 层

1. **新增 `MetadataModelVersionDO`** (文件: `stock-common/.../entity/MetadataModelVersionDO.java`)
   - 动作: 创建 DO 类，包含 id, modelCode, modelId, version, schemaContent, versionDesc, gmtCreate, gmtModified
   - 依赖: 无

2. **修改 `MetadataModelDO`** (文件: `stock-common/.../entity/MetadataModelDO.java`)
   - 动作: 新增两个字段
     ```java
     private Integer currentVersion;  // 当前生效版本（0=最新版）
     private String snapshotHash;     // 最近一次发布时的工作区 hash
     ```

3. **新增 `MetadataModelVersionMapper` 接口** (文件: `stock-common/.../mappers/MetadataModelVersionMapper.java`)
   - 动作: 创建 Mapper 接口
     ```java
     @Mapper
     public interface MetadataModelVersionMapper {
         int insert(MetadataModelVersionDO versionDO);
         List<MetadataModelVersionDO> selectByModelId(Long modelId);
         MetadataModelVersionDO selectByModelIdAndVersion(Long modelId, Integer version);
         Integer selectMaxVersionByModelId(Long modelId);
     }
     ```

4. **新增 `MetadataModelVersionMapper.xml`** (文件: `stock-common/.../resources/mappers/MetadataModelVersionMapper.xml`)
   - SQL:
     - `insert`: `insert into metadata_model_version (model_code, model_id, version, schema_content, version_desc, gmt_create, gmt_modified) values (...)` with `useGeneratedKeys`
     - `selectByModelId`: `select * from metadata_model_version where model_id = #{modelId} order by version desc`
     - `selectByModelIdAndVersion`: `select * ... where model_id = #{modelId} and version = #{version}`
     - `selectMaxVersionByModelId`: `select coalesce(max(version), 0) from metadata_model_version where model_id = #{modelId}`

5. **修改 `MetadataModelMapper.xml`** (文件: `stock-common/.../resources/mappers/MetadataModelMapper.xml`)
   - `BaseResultMap` 新增 `<result column="current_version" property="currentVersion"/>` 和 `<result column="snapshot_hash" property="snapshotHash"/>`
   - `updateById` 新增 `current_version = #{currentVersion}, snapshot_hash = #{snapshotHash}`

6. **修改 `init-schema.sql`** (文件: `stock-integration-test/.../init-schema.sql`)
   - `metadata_model` 表定义中新增 `current_version INT DEFAULT 0`, `snapshot_hash VARCHAR(64) DEFAULT NULL`
   - 新增 `metadata_model_version` 建表语句

### 阶段2: Domain Service 层

1. **修改 `MetadataModel` 领域模型** (文件: `stock-core/.../model/metadata/MetadataModel.java`)
   - 新增 `private Integer currentVersion; private String snapshotHash;`

2. **新增 `MetadataModelVersion` 领域模型** (文件: `stock-core/.../model/metadata/MetadataModelVersion.java`)
   - `Long id, String modelCode, Long modelId, Integer version, Map<String,Object> schemaContent, String versionDesc, Date gmtCreate, Date gmtModified`

3. **修改 `DomainConverter`** (文件: `stock-core/.../converter/DomainConverter.java`)
   - `toDomain(MetadataModelDO)`: 补充 `domain.setCurrentVersion(...)`, `domain.setSnapshotHash(...)`
   - 新增 `toDomain(MetadataModelVersionDO)`: DO -> Domain

4. **修改 `DbConverter`** (文件: `stock-core/.../converter/DbConverter.java`)
   - `toDbEntity(MetadataModel)`: 补充 `dbEntity.setCurrentVersion(...)`, `dbEntity.setSnapshotHash(...)`
   - 新增 `toDbEntity(MetadataModelVersion)`: Domain -> DO

5. **修改 `MetadataDomainService` 接口** (文件: `stock-core/.../service/MetadataDomainService.java`)
   - `publishModel(Long modelId)` 改为 `publishModel(Long modelId, String versionDesc)`
   - 新增 4 个方法:
     ```java
     List<MetadataModelVersion> listModelVersions(Long modelId);
     void switchModelVersion(Long modelId, Integer version);
     Map<String, Object> getSchemaByVersion(Long modelId, Integer version);
     Map<String, Object> getModelSchema(Long modelId);
     ```

6. **重写 `MetadataDomainServiceImpl`** (文件: `stock-core/.../service/impl/MetadataDomainServiceImpl.java`)
   - 注入 `MetadataModelVersionMapper`
   - 新增 `computeModelHash(Long modelId)`: 模型属性 + 排序后字段 -> JSON -> SHA-256 -> 前32字符
   - 新增 `refreshModelStatus(Long modelId)`: saveModel/saveField 后调用，对比 hash 更新状态
   - 重写 `publishModel`: @Transactional, 校验 -> 取 maxVersion+1 -> 计算 JSONSchema -> 插入版本 -> 计算 hash -> 更新 status/snapshot_hash
   - 实现 `listModelVersions`, `switchModelVersion`, `getSchemaByVersion`, `getModelSchema`
   - 注意: 从 validateSchema 中提取纯校验逻辑 `doValidateSchema`（不更新 status），publishModel 内部调用此方法

### 阶段3: Controller + DTO 层

1. **新增 DTO 类** (在 `stock-biz/.../dto/`)
   - `ModelVersionDTO`: id, version, versionDesc, isCurrent, gmtCreate
   - `SwitchVersionRequest`: @NotNull Long modelId, @NotNull Integer version
   - `PublishModelRequest`: @NotNull Long modelId, @Size(max=512) String versionDesc

2. **修改 `MetadataModelDTO`**: 新增 `currentVersion`, `versions` 字段

3. **修改 `ErrorCode`**: 新增 `METADATA_VERSION_NOT_FOUND(42006, "模型版本不存在")`

4. **修改 `DtoConverter`**: 新增 `toDto(MetadataModelVersion)` -> ModelVersionDTO, `toDto(MetadataModel)` 补充 currentVersion

5. **修改 `MetadataController`**:
   - `publishModel` 改为接收 `PublishModelRequest`
   - 新增 `GET /model/versions.json?id={}` -> `listModelVersions`
   - 新增 `POST /model/switch-version.json` -> `switchModelVersion`
   - 修改 `GET /model/schema.json?id={}&version={}` 支持可选 version 参数

### 阶段4: 集成测试

1. 修改 `init-schema.sql` 包含新表
2. 在 `MetadataIT` 新增 6 个验收测试用例 (AC1-AC6)

### 阶段5: 前端

1. **修改 `services/metadata.js`**: 新增 `listModelVersions`, `switchModelVersion`, 修改 `publishModel` 加 versionDesc
2. **修改 `ModelDetail/index.jsx`**:
   - STATUS_MAP 新增 CHANGING
   - 版本历史按钮 + Drawer 版本列表 + 切换按钮

## 测试策略

- 集成测试覆盖发布 -> 变更检测 -> 版本切换 -> JSONSchema 缓存完整流程
- 使用 H2 内存数据库 + TestRestTemplate
- 7 个验收条件全部通过

## 风险与缓解

- publishModel 内部调用 validateSchema 的侧边更新问题: 提取私有方法 doValidateSchema
- 并发发布唯一索引冲突: 唯一索引兜底
- 前端发布请求体格式兼容: 新增可选字段 versionDesc，无破坏性变更
- 旧 schema.json 端点兼容: @RequestParam(required=false) 确保向后兼容