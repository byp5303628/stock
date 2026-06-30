# 元数据模型版本化与 JSONSchema 缓存 — 测试用例

## 测试概览

| 维度 | 内容 |
|------|------|
| 接口总数 | 8 |
| 接口用例数 | 22 |
| 端到端场景数 | 7（对应 AC1-AC7） |
| 覆盖模块 | 模型发布、版本列表、版本切换、JSONSchema 查询、模型列表、模型保存、模型详情、字段保存 |

---

## 接口测试

### API-1 POST /api/metadata/model/publish.json

**接口说明**

- 方法：POST
- 路径：`/api/metadata/model/publish.json`
- 说明：发布模型，校验通过后生成新版本，计算 JSONSchema 写入缓存，模型状态置为 PUBLISHED
- 请求体：`PublishModelRequest`（`modelId`: Long，`versionDesc`: String 可选）

---

#### 用例 1-1：首次发布 DRAFT 模型

**说明**：对一个 DRAFT 状态、从未发布的模型执行发布，预期生成 version=1，状态变为 PUBLISHED，schema_content 非空。

`{modelId}` 的获取方式：调用 `POST /api/metadata/model/save.json` 创建模型后从响应的 `data.id` 字段取得，或查询测试数据准备章节中插入的 DRAFT 模型 ID。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/publish.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {modelId},
    "versionDesc": "首次发布 v1"
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "version": 1,
    "status": "PUBLISHED",
    "schemaContent": "<非空 JSON Schema 字符串>"
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] `data.version` 为 `1`
- [ ] `data.status` 为 `"PUBLISHED"`
- [ ] `data.schemaContent` 不为空且是合法 JSON
- [ ] DB 查询：`SELECT version, schema_content FROM metadata_model_version WHERE model_code = '{modelCode}' AND version = 1` → 存在一条记录，`schema_content` 非空
- [ ] DB 查询：`SELECT status, published_snapshot FROM metadata_model WHERE id = {modelId}` → `status='PUBLISHED'`，`published_snapshot` 非空

---

#### 用例 1-2：对已有版本的模型再次发布（CHANGING 状态）

**说明**：已发布过（version=1），修改字段后状态变为 CHANGING，再次发布应生成 version=2。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/publish.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {modelId},
    "versionDesc": "新增字段后的第二版"
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "version": 2,
    "status": "PUBLISHED",
    "schemaContent": "<包含新增字段的 JSON Schema>"
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `data.version` 为 `2`
- [ ] `data.status` 为 `"PUBLISHED"`
- [ ] DB 查询：`SELECT COUNT(*) FROM metadata_model_version WHERE model_code = '{modelCode}'` → 返回 2
- [ ] DB 查询 version=2 的记录 `schema_content` 与 version=1 的记录内容不同（字段已变化）

---

#### 用例 1-3：发布 Schema 校验失败的模型

**说明**：模型字段配置不合法（如必填字段缺少类型），校验失败时不生成版本，模型状态不变。

`{invalidModelId}` 指向一个字段配置不合法的模型（如字段 `fieldType` 为空）。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/publish.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {invalidModelId},
    "versionDesc": "应该失败的发布"
  }'
```

**预期 HTTP 状态码**：200（使用统一响应封装，业务失败通过 success=false 表达）

**预期响应 JSON**：
```json
{
  "success": false,
  "data": null,
  "errorCode": "METADATA_SCHEMA_VALIDATE_FAIL",
  "errorMsg": "<校验失败原因描述>"
}
```

**验证点**：
- [ ] `success` 为 `false`
- [ ] `errorCode` 非空，包含校验失败语义（如 `VALIDATE_FAIL` 或 `SCHEMA_INVALID`）
- [ ] DB 查询：`SELECT COUNT(*) FROM metadata_model_version WHERE model_code = '{invalidModelCode}'` → 返回 0（未插入新版本）
- [ ] DB 查询：`SELECT status FROM metadata_model WHERE id = {invalidModelId}` → 状态与发布前相同，未改变

---

#### 用例 1-4：modelId 不存在

**说明**：传入不存在的 modelId，接口应返回模型不存在的错误。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/publish.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": 999999999,
    "versionDesc": "不存在的模型"
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": false,
  "data": null,
  "errorCode": "METADATA_MODEL_NOT_FOUND",
  "errorMsg": "模型不存在"
}
```

**验证点**：
- [ ] `success` 为 `false`
- [ ] `errorCode` 包含模型不存在语义
- [ ] 无新版本记录写入

---

### API-2 GET /api/metadata/model/versions.json

**接口说明**

- 方法：GET
- 路径：`/api/metadata/model/versions.json`
- 说明：查询指定模型的所有版本列表，按版本号降序排列
- 请求参数：`id`（modelId，Long，必填）

---

#### 用例 2-1：查询有多个版本的模型

**说明**：模型已有 version=1 和 version=2，返回列表应按版本号降序，最新版本在首位，并标识当前生效版本。

`{modelId}` 为测试数据准备章节中插入的 PUBLISHED 状态模型 ID。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/versions.json?id={modelId}" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": [
    {
      "version": 2,
      "versionDesc": "第二版",
      "isCurrent": true,
      "gmtCreate": "<ISO 8601 时间戳>"
    },
    {
      "version": 1,
      "versionDesc": "首次发布",
      "isCurrent": false,
      "gmtCreate": "<ISO 8601 时间戳>"
    }
  ],
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] 列表长度为 2
- [ ] 第一个元素 `version` 为 2（降序）
- [ ] `isCurrent=true` 的项 `version` 与 DB 中 `metadata_model.current_version` 一致
- [ ] 每个版本项包含 `version`、`versionDesc`、`gmtCreate` 字段

---

#### 用例 2-2：查询从未发布的 DRAFT 模型

**说明**：DRAFT 模型没有任何版本记录，返回空列表。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/versions.json?id={draftModelId}" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": [],
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] `data` 为空数组 `[]`

---

#### 用例 2-3：模型 ID 不存在

**说明**：传入不存在的 modelId，接口返回错误。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/versions.json?id=999999999" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": false,
  "data": null,
  "errorCode": "METADATA_MODEL_NOT_FOUND",
  "errorMsg": "模型不存在"
}
```

**验证点**：
- [ ] `success` 为 `false`
- [ ] `errorCode` 包含模型不存在语义

---

### API-3 POST /api/metadata/model/switch-version.json

**接口说明**

- 方法：POST
- 路径：`/api/metadata/model/switch-version.json`
- 说明：切换模型的当前生效版本（仅更新 current_version 指针，不修改工作区）
- 请求体：`SwitchVersionRequest`（`modelId`: Long，`version`: Integer）

---

#### 用例 3-1：正常切换到旧版本

**说明**：模型当前 current_version=2，切换到 version=1，DB 中 current_version 应更新为 1。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/switch-version.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {modelId},
    "version": 1
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "modelId": "{modelId}",
    "currentVersion": 1
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] `data.currentVersion` 为 `1`
- [ ] DB 查询：`SELECT current_version FROM metadata_model WHERE id = {modelId}` → 返回 `1`
- [ ] 模型工作区（属性、字段）未发生变化

---

#### 用例 3-2：切换到不存在的版本号

**说明**：目标版本号在 metadata_model_version 中不存在，接口应返回版本不存在的错误。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/switch-version.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {modelId},
    "version": 999
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": false,
  "data": null,
  "errorCode": "METADATA_VERSION_NOT_FOUND",
  "errorMsg": "指定版本不存在"
}
```

**验证点**：
- [ ] `success` 为 `false`
- [ ] `errorCode` 包含版本不存在语义
- [ ] DB 查询：`SELECT current_version FROM metadata_model WHERE id = {modelId}` → 值未变化

---

#### 用例 3-3：模型 ID 不存在

**说明**：传入不存在的 modelId。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/switch-version.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": 999999999,
    "version": 1
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": false,
  "data": null,
  "errorCode": "METADATA_MODEL_NOT_FOUND",
  "errorMsg": "模型不存在"
}
```

**验证点**：
- [ ] `success` 为 `false`
- [ ] `errorCode` 包含模型不存在语义

---

### API-4 GET /api/metadata/model/schema.json

**接口说明**

- 方法：GET
- 路径：`/api/metadata/model/schema.json`
- 说明：查询 JSONSchema，优先从 DB 缓存读取，缓存缺失时 fallback 实时计算
- 请求参数：`id`（modelId，Long，必填），`version`（Integer，可选，不传则取 current_version）

---

#### 用例 4-1：查询指定版本的 JSONSchema（命中 DB 缓存）

**说明**：模型已发布 version=1，version 对应的 schema_content 非空，接口应直接返回 DB 缓存内容。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/schema.json?id={modelId}&version=1" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "version": 1,
    "schema": {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {}
    },
    "fromCache": true
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] `data.schema` 是合法的 JSON Schema 对象（含 `$schema` 或 `type` 字段）
- [ ] `data.version` 为 `1`
- [ ] 响应内容与 DB 中 `metadata_model_version.schema_content`（version=1）一致
- [ ] 响应时间 < 50ms（缓存读取，无需实时计算）

---

#### 用例 4-2：不传 version 参数，取当前生效版本

**说明**：不传 version，接口应读取 `metadata_model.current_version` 对应的版本 schema。

当 `current_version=2` 时：

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/schema.json?id={modelId}" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "version": 2,
    "schema": {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {}
    },
    "fromCache": true
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `data.version` 与 DB 中 `metadata_model.current_version` 值一致
- [ ] `data.schema` 与 `metadata_model_version.schema_content`（对应 current_version）内容一致

---

#### 用例 4-3：查询不存在的版本号

**说明**：指定版本号在 metadata_model_version 中不存在。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/schema.json?id={modelId}&version=999" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": false,
  "data": null,
  "errorCode": "METADATA_VERSION_NOT_FOUND",
  "errorMsg": "指定版本不存在"
}
```

**验证点**：
- [ ] `success` 为 `false`
- [ ] `errorCode` 包含版本不存在语义

---

#### 用例 4-4：schema_content 为空时 fallback 实时计算

**说明**：版本记录存在但 `schema_content` 为空（例如历史数据遗留），接口应 fallback 实时计算并返回。

准备：直接在 DB 中插入一条 `schema_content=NULL` 的版本记录（见测试数据准备章节）。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/schema.json?id={modelId}&version={emptySchemaVersion}" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "version": "{emptySchemaVersion}",
    "schema": {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {}
    },
    "fromCache": false
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] `data.schema` 为非空的合法 JSON Schema
- [ ] `data.fromCache` 为 `false`（如接口有此字段）或通过响应时间判断（实时计算通常 > 50ms）

---

### API-5 GET /api/metadata/model/list.json

**接口说明**

- 方法：GET
- 路径：`/api/metadata/model/list.json`
- 说明：查询模型列表，需在响应中展示新增的 `status` 和 `currentVersion` 字段

---

#### 用例 5-1：列表返回包含新增字段

**说明**：确认列表接口在引入版本化后，返回的每个模型包含 `status` 和 `currentVersion` 字段。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/list.json" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**（关键字段）：
```json
{
  "success": true,
  "data": [
    {
      "id": "{modelId}",
      "modelCode": "{modelCode}",
      "name": "{modelName}",
      "status": "PUBLISHED",
      "currentVersion": 2
    }
  ],
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] 每个模型对象包含 `status` 字段，值为 `DRAFT`、`CHANGING` 或 `PUBLISHED`
- [ ] 每个模型对象包含 `currentVersion` 字段（DRAFT 模型为 `null`，已发布模型为整数）
- [ ] PUBLISHED 状态的模型 `currentVersion` 非空

---

#### 用例 5-2：DRAFT 状态模型 currentVersion 为 null

**说明**：从未发布的 DRAFT 模型在列表中 `currentVersion` 应为 null。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/list.json" \
  -H "Content-Type: application/json"
```

**预期响应（关键字段）**：
```json
{
  "success": true,
  "data": [
    {
      "id": "{draftModelId}",
      "status": "DRAFT",
      "currentVersion": null
    }
  ]
}
```

**验证点**：
- [ ] DRAFT 状态模型 `currentVersion` 为 `null`
- [ ] DB 查询：`SELECT current_version FROM metadata_model WHERE id = {draftModelId}` → 返回 `NULL`

---

### API-6 POST /api/metadata/model/save.json

**接口说明**

- 方法：POST
- 路径：`/api/metadata/model/save.json`
- 说明：保存模型属性，保存后自动触发变更检测，重新计算哈希与 published_snapshot 对比

---

#### 用例 6-1：PUBLISHED 状态模型修改属性后触发变更检测，状态变为 CHANGING

**说明**：已发布模型修改 `description`，保存后哈希发生变化，状态自动置为 CHANGING。

`{modelId}` 为 PUBLISHED 状态模型 ID，`{modelCode}` 为其 model_code。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/save.json" \
  -H "Content-Type: application/json" \
  -d '{
    "id": {modelId},
    "modelCode": "{modelCode}",
    "name": "修改后的模型名称",
    "description": "新增描述触发变更检测"
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "id": "{modelId}",
    "status": "CHANGING"
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] `data.status` 为 `"CHANGING"`
- [ ] DB 查询：`SELECT status FROM metadata_model WHERE id = {modelId}` → 返回 `CHANGING`

---

#### 用例 6-2：DRAFT 状态模型保存属性，状态保持 DRAFT

**说明**：DRAFT 状态模型从未发布，保存属性后不触发 CHANGING 检测，状态保持 DRAFT。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/model/save.json" \
  -H "Content-Type: application/json" \
  -d '{
    "id": {draftModelId},
    "modelCode": "{draftModelCode}",
    "name": "草稿模型更新名称",
    "description": "草稿状态不应变为 CHANGING"
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "id": "{draftModelId}",
    "status": "DRAFT"
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `data.status` 为 `"DRAFT"`（未发布过，修改属性不触发 CHANGING）
- [ ] DB 查询：`SELECT status FROM metadata_model WHERE id = {draftModelId}` → 返回 `DRAFT`

---

### API-7 GET /api/metadata/model/detail.json

**接口说明**

- 方法：GET
- 路径：`/api/metadata/model/detail.json`
- 说明：查询模型详情，增强后返回 `currentVersion`、`status` 字段
- 请求参数：`id`（modelId，Long，必填）

---

#### 用例 7-1：查询 PUBLISHED 状态模型详情

**说明**：已发布模型详情应包含 `status=PUBLISHED`、`currentVersion` 非空。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/detail.json?id={modelId}" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**（关键字段）：
```json
{
  "success": true,
  "data": {
    "id": "{modelId}",
    "modelCode": "{modelCode}",
    "name": "{modelName}",
    "status": "PUBLISHED",
    "currentVersion": 2,
    "fields": []
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `data.status` 为 `"PUBLISHED"`
- [ ] `data.currentVersion` 非空且为正整数
- [ ] `data.fields` 为模型的字段列表（非空）

---

#### 用例 7-2：查询 DRAFT 状态模型详情

**说明**：DRAFT 状态模型 `currentVersion` 应为 null。

```bash
curl -s -X GET "http://localhost:8080/api/metadata/model/detail.json?id={draftModelId}" \
  -H "Content-Type: application/json"
```

**预期 HTTP 状态码**：200

**预期响应 JSON**（关键字段）：
```json
{
  "success": true,
  "data": {
    "id": "{draftModelId}",
    "status": "DRAFT",
    "currentVersion": null
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `data.status` 为 `"DRAFT"`
- [ ] `data.currentVersion` 为 `null`

---

### API-8 POST /api/metadata/field/save.json

**接口说明**

- 方法：POST
- 路径：`/api/metadata/field/save.json`
- 说明：保存字段，保存后触发变更检测，重新计算哈希与 published_snapshot 对比

---

#### 用例 8-1：PUBLISHED 状态模型新增字段后状态变为 CHANGING

**说明**：对已发布模型新增一个字段，保存后哈希与 published_snapshot 不一致，状态变为 CHANGING。

```bash
curl -s -X POST "http://localhost:8080/api/metadata/field/save.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {modelId},
    "fieldName": "new_field_for_change_detection",
    "fieldType": "STRING",
    "required": false,
    "sortOrder": 99
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "modelId": "{modelId}",
    "modelStatus": "CHANGING"
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] 响应中体现模型状态为 `"CHANGING"`（可能在 data.modelStatus 或 data.model.status）
- [ ] DB 查询：`SELECT status FROM metadata_model WHERE id = {modelId}` → 返回 `CHANGING`

---

#### 用例 8-2：修改字段恢复到与 published_snapshot 一致，状态变为 PUBLISHED

**说明**：已发布模型 (version=1) 修改了一个字段导致 CHANGING，再将该字段改回原始值，哈希重新与 published_snapshot 一致，状态应自动变回 PUBLISHED。

**前置操作**：
1. 确认模型当前 `status=CHANGING`，`published_snapshot=<hash_A>`
2. 将字段恢复到发布前的值

```bash
curl -s -X POST "http://localhost:8080/api/metadata/field/save.json" \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": {modelId},
    "fieldId": {existingFieldId},
    "fieldName": "original_field_name",
    "fieldType": "STRING",
    "required": false,
    "sortOrder": 1
  }'
```

**预期 HTTP 状态码**：200

**预期响应 JSON**：
```json
{
  "success": true,
  "data": {
    "modelId": "{modelId}",
    "modelStatus": "PUBLISHED"
  },
  "errorCode": null,
  "errorMsg": null
}
```

**验证点**：
- [ ] `success` 为 `true`
- [ ] 模型状态变回 `"PUBLISHED"`
- [ ] DB 查询：`SELECT status FROM metadata_model WHERE id = {modelId}` → 返回 `PUBLISHED`

---

## 端到端测试

### E2E-1 AC1：DRAFT 模型首次发布生成 version=1

**前置条件**

- DB 中不存在 model_code 为 `test-model-{timestamp}` 的模型
- 时间戳格式：Unix 毫秒，例如 `test-model-1751000000000`

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 创建新模型：`POST /api/metadata/model/save.json`，body 含 `modelCode: "test-model-{timestamp}"`，`name: "E2E测试模型"` | 接口响应 | `success=true`，返回 `data.id`（记为 `E1_MODEL_ID`），`data.status="DRAFT"` |
| 2 | 添加字段：`POST /api/metadata/field/save.json`，body 含 `modelId: E1_MODEL_ID`，`fieldName: "title"`，`fieldType: "STRING"` | 接口响应 | `success=true`，字段保存成功 |
| 3 | 添加字段：`POST /api/metadata/field/save.json`，body 含 `modelId: E1_MODEL_ID`，`fieldName: "amount"`，`fieldType: "NUMBER"` | 接口响应 | `success=true`，字段保存成功 |
| 4 | 查询详情：`GET /api/metadata/model/detail.json?id=E1_MODEL_ID` | 接口响应 | `status="DRAFT"`，`currentVersion=null` |
| 5 | 发布模型：`POST /api/metadata/model/publish.json`，body: `{modelId: E1_MODEL_ID, versionDesc: "E2E首次发布"}` | 接口响应 | `success=true`，`data.version=1`，`data.status="PUBLISHED"` |
| 6 | 查询版本列表：`GET /api/metadata/model/versions.json?id=E1_MODEL_ID` | 接口响应 | 列表长度为 1，第一个元素 `version=1` |
| 7 | 查询 Schema：`GET /api/metadata/model/schema.json?id=E1_MODEL_ID&version=1` | 接口响应 | `data.schema` 非空，含 `title`、`amount` 字段定义 |

**收尾验证**

```sql
-- 验证版本记录存在且 schema_content 非空
SELECT version, schema_content, version_desc
FROM metadata_model_version
WHERE model_code = 'test-model-{timestamp}';
-- 预期：返回 1 行，version=1，schema_content 非空，version_desc='E2E首次发布'

-- 验证模型状态
SELECT status, current_version, published_snapshot
FROM metadata_model
WHERE id = {E1_MODEL_ID};
-- 预期：status='PUBLISHED'，current_version=NULL（发布不自动切换），published_snapshot 非空
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E1_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E1_MODEL_ID};
```

---

### E2E-2 AC2：已发布模型修改字段后状态变为 CHANGING

**前置条件**

- 存在一个已发布模型（model_code: `test-model-e2-{timestamp}`，version=1，status=PUBLISHED）
- 可复用 E2E-1 发布完成后的模型，或使用测试数据准备章节中的 PUBLISHED 模型

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 确认当前状态：`GET /api/metadata/model/detail.json?id={E2_MODEL_ID}` | 接口响应 | `status="PUBLISHED"` |
| 2 | 新增字段：`POST /api/metadata/field/save.json`，body 含 `modelId: E2_MODEL_ID`，`fieldName: "extra_field"`，`fieldType: "STRING"` | 接口响应 | `success=true` |
| 3 | 查询详情验证状态变更：`GET /api/metadata/model/detail.json?id={E2_MODEL_ID}` | 接口响应 | `status="CHANGING"` |
| 4 | 修改现有字段：`POST /api/metadata/field/save.json`，修改已有字段的 `required` 值 | 接口响应 | `success=true` |
| 5 | 再次查询详情确认状态不变：`GET /api/metadata/model/detail.json?id={E2_MODEL_ID}` | 接口响应 | `status="CHANGING"`（已是 CHANGING，继续保持） |

**收尾验证**

```sql
-- 验证状态已变为 CHANGING
SELECT status, published_snapshot
FROM metadata_model
WHERE id = {E2_MODEL_ID};
-- 预期：status='CHANGING'，published_snapshot 仍为 version=1 发布时的哈希值（未变化）

-- 验证版本数量未增加（修改字段不自动发布新版本）
SELECT COUNT(*) FROM metadata_model_version WHERE model_code = 'test-model-e2-{timestamp}';
-- 预期：COUNT=1（依然只有 version=1）
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-e2-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E2_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E2_MODEL_ID};
```

---

### E2E-3 AC3：CHANGING 状态发布，版本号递增为 2

**前置条件**

- 存在一个 CHANGING 状态模型（已有 version=1，在 E2E-2 执行完毕后或手动准备）
- model_code: `test-model-e3-{timestamp}`

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 确认当前状态：`GET /api/metadata/model/detail.json?id={E3_MODEL_ID}` | 接口响应 | `status="CHANGING"`，`currentVersion=null` 或之前的版本值 |
| 2 | 查询当前版本列表：`GET /api/metadata/model/versions.json?id={E3_MODEL_ID}` | 接口响应 | 列表长度 ≥ 1，最大版本号为 1 |
| 3 | 发布模型：`POST /api/metadata/model/publish.json`，body: `{modelId: E3_MODEL_ID, versionDesc: "CHANGING状态发布v2"}` | 接口响应 | `success=true`，`data.version=2`，`data.status="PUBLISHED"` |
| 4 | 查询版本列表：`GET /api/metadata/model/versions.json?id={E3_MODEL_ID}` | 接口响应 | 列表长度为 2，第一个元素 `version=2`（降序） |
| 5 | 查询模型详情：`GET /api/metadata/model/detail.json?id={E3_MODEL_ID}` | 接口响应 | `status="PUBLISHED"` |

**收尾验证**

```sql
-- 验证 version=2 记录存在
SELECT version, schema_content FROM metadata_model_version
WHERE model_code = 'test-model-e3-{timestamp}' AND version = 2;
-- 预期：返回 1 行，schema_content 非空

-- 验证模型状态
SELECT status FROM metadata_model WHERE id = {E3_MODEL_ID};
-- 预期：status='PUBLISHED'
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-e3-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E3_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E3_MODEL_ID};
```

---

### E2E-4 AC4：版本切换后 current_version 更新为目标版本

**前置条件**

- 存在一个模型，已有 version=1 和 version=2，当前 current_version=2（或 null）
- model_code: `test-model-e4-{timestamp}`

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 查询版本列表：`GET /api/metadata/model/versions.json?id={E4_MODEL_ID}` | 接口响应 | 列表包含 version=1 和 version=2，当前高亮版本为较新版 |
| 2 | 切换到 version=1：`POST /api/metadata/model/switch-version.json`，body: `{modelId: E4_MODEL_ID, version: 1}` | 接口响应 | `success=true`，`data.currentVersion=1` |
| 3 | 再次查询版本列表：`GET /api/metadata/model/versions.json?id={E4_MODEL_ID}` | 接口响应 | version=1 的 `isCurrent=true`，version=2 的 `isCurrent=false` |
| 4 | 查询 Schema（不传 version）：`GET /api/metadata/model/schema.json?id={E4_MODEL_ID}` | 接口响应 | 返回 version=1 的 schema（与 DB 中 version=1 的 schema_content 一致） |
| 5 | 查询模型详情：`GET /api/metadata/model/detail.json?id={E4_MODEL_ID}` | 接口响应 | `currentVersion=1` |

**收尾验证**

```sql
-- 验证 current_version 已更新为 1
SELECT current_version FROM metadata_model WHERE id = {E4_MODEL_ID};
-- 预期：current_version=1

-- 验证工作区字段未受影响（字段数量与切换前相同）
SELECT COUNT(*) FROM metadata_field WHERE model_id = {E4_MODEL_ID};
-- 预期：字段数量与切换前相同（切换版本不修改工作区）
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-e4-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E4_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E4_MODEL_ID};
```

---

### E2E-5 AC5：查询指定版本 schema.json 返回 DB 缓存内容

**前置条件**

- 存在一个模型，已发布 version=2，两个版本的 schema_content 均非空
- model_code: `test-model-e5-{timestamp}`

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 从 DB 直接读取 version=2 的 schema_content（作为期望值）：查询 `SELECT schema_content FROM metadata_model_version WHERE model_code='test-model-e5-{timestamp}' AND version=2` | DB 直接查询 | 返回一条 schema_content 字符串（记为 `EXPECTED_SCHEMA`） |
| 2 | 查询接口：`GET /api/metadata/model/schema.json?id={E5_MODEL_ID}&version=2` | 接口响应 | `success=true`，`data.schema` 内容与 `EXPECTED_SCHEMA` 一致 |
| 3 | 查询 version=1 的 schema：`GET /api/metadata/model/schema.json?id={E5_MODEL_ID}&version=1` | 接口响应 | 返回 version=1 的 schema，与 version=2 的 schema 内容不同（字段有差异） |
| 4 | 多次请求相同接口，确认响应稳定 | 接口响应 | 每次返回内容相同（无随机性） |

**收尾验证**

```sql
-- 验证 DB 中两个版本均有 schema_content
SELECT version, LENGTH(schema_content) as schema_len
FROM metadata_model_version
WHERE model_code = 'test-model-e5-{timestamp}'
ORDER BY version;
-- 预期：返回 2 行，两个版本 schema_len 均 > 0
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-e5-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E5_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E5_MODEL_ID};
```

---

### E2E-6 AC6：发布后工作区字段和属性不受影响

**前置条件**

- 存在一个 CHANGING 状态模型，工作区有 3 个字段（field_a、field_b、field_c），其中 field_c 是在 version=1 发布后新增的
- model_code: `test-model-e6-{timestamp}`

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 查询发布前工作区字段：`GET /api/metadata/model/detail.json?id={E6_MODEL_ID}` | 接口响应 | `data.fields` 包含 3 个字段：field_a、field_b、field_c |
| 2 | 记录发布前字段的所有属性值（fieldName、fieldType、required、sortOrder 等）| 本地记录 | 记录为 `PRE_PUBLISH_FIELDS` |
| 3 | 发布模型：`POST /api/metadata/model/publish.json`，body: `{modelId: E6_MODEL_ID, versionDesc: "验证发布不改变工作区"}` | 接口响应 | `success=true`，`data.version=2`，`data.status="PUBLISHED"` |
| 4 | 查询发布后工作区字段：`GET /api/metadata/model/detail.json?id={E6_MODEL_ID}` | 接口响应 | `data.fields` 依然包含 3 个字段，field_c 仍然存在 |
| 5 | 逐一对比发布前后字段属性 | 本地对比 | 所有字段的 fieldName、fieldType、required、sortOrder 与 `PRE_PUBLISH_FIELDS` 完全一致 |

**收尾验证**

```sql
-- 验证字段数量未变
SELECT COUNT(*) FROM metadata_field WHERE model_id = {E6_MODEL_ID};
-- 预期：COUNT=3（field_a、field_b、field_c 均在）

-- 验证 version=2 的 schema_content 包含 field_c（发布时基于当前工作区）
SELECT schema_content FROM metadata_model_version
WHERE model_code = 'test-model-e6-{timestamp}' AND version = 2;
-- 预期：schema_content 中包含 "field_c" 字符串
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-e6-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E6_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E6_MODEL_ID};
```

---

### E2E-7 AC7：版本列表按版本号降序，当前版本高亮

**前置条件**

- 存在一个模型，已发布 3 个版本（version=1、2、3），current_version=2
- model_code: `test-model-e7-{timestamp}`

**执行步骤**

| 步骤 | 操作 | 验证方式 | 预期结果 |
|------|------|---------|---------|
| 1 | 查询版本列表：`GET /api/metadata/model/versions.json?id={E7_MODEL_ID}` | 接口响应 | `success=true`，`data` 数组长度为 3 |
| 2 | 检查列表顺序 | 接口响应 | 第 1 个元素 `version=3`，第 2 个元素 `version=2`，第 3 个元素 `version=1`（降序） |
| 3 | 检查当前版本标记 | 接口响应 | `version=2` 的元素 `isCurrent=true`，version=1 和 version=3 的 `isCurrent=false` |
| 4 | 切换当前版本到 version=3：`POST /api/metadata/model/switch-version.json`，body: `{modelId: E7_MODEL_ID, version: 3}` | 接口响应 | `success=true` |
| 5 | 再次查询版本列表 | 接口响应 | `version=3` 的元素 `isCurrent=true`，其余为 `false` |
| 6 | 检查每个版本项包含必要字段 | 接口响应 | 每项含 `version`（整数）、`versionDesc`（字符串）、`gmtCreate`（时间戳）、`isCurrent`（布尔）|

**收尾验证**

```sql
-- 验证有 3 个版本记录且按 version 降序
SELECT version, version_desc, gmt_create
FROM metadata_model_version
WHERE model_code = 'test-model-e7-{timestamp}'
ORDER BY version DESC;
-- 预期：返回 3 行，version 分别为 3、2、1

-- 验证 current_version 已切换到 3
SELECT current_version FROM metadata_model WHERE id = {E7_MODEL_ID};
-- 预期：current_version=3
```

**清理**

```sql
DELETE FROM metadata_model_version WHERE model_code = 'test-model-e7-{timestamp}';
DELETE FROM metadata_field WHERE model_id = {E7_MODEL_ID};
DELETE FROM metadata_model WHERE id = {E7_MODEL_ID};
```

---

## 测试数据准备

以下 SQL 可直接执行，用于创建端到端测试和接口测试所需的初始数据。执行前请确认 MySQL 连接正常，并替换 `{timestamp}` 为当前 Unix 毫秒时间戳。

```sql
-- ============================================================
-- 测试数据准备：元数据模型版本化与 JSONSchema 缓存
-- 执行前将 {timestamp} 替换为当前毫秒时间戳，例如 1751000000000
-- ============================================================

-- -------------------------------------------------------
-- 数据集 A：DRAFT 状态模型（从未发布，用于 AC1 / API-1 / API-5 / API-6 / API-7 基础测试）
-- -------------------------------------------------------

-- A1：插入 DRAFT 状态模型
INSERT INTO metadata_model (model_code, name, description, status, current_version, published_snapshot, gmt_create, gmt_modified)
VALUES (
    'test-draft-{timestamp}',       -- model_code：语义主键，VARCHAR
    'E2E草稿测试模型',
    '用于测试首次发布和变更检测的草稿模型',
    'DRAFT',                        -- 草稿状态：从未发布
    NULL,                           -- current_version 为 null：从未发布
    NULL,                           -- published_snapshot 为 null：从未发布
    NOW(),
    NOW()
);

-- A2：查询刚插入的 DRAFT 模型 ID（后续步骤中使用）
-- SELECT id FROM metadata_model WHERE model_code = 'test-draft-{timestamp}';
-- 将返回的 id 替换到后续 SQL 和 curl 命令中的 {draftModelId}

-- A3：为 DRAFT 模型插入 2 个字段
INSERT INTO metadata_field (model_id, field_name, field_type, required, sort_order, gmt_create, gmt_modified)
SELECT id, 'title', 'STRING', 1, 1, NOW(), NOW()
FROM metadata_model WHERE model_code = 'test-draft-{timestamp}';

INSERT INTO metadata_field (model_id, field_name, field_type, required, sort_order, gmt_create, gmt_modified)
SELECT id, 'amount', 'NUMBER', 0, 2, NOW(), NOW()
FROM metadata_model WHERE model_code = 'test-draft-{timestamp}';

-- -------------------------------------------------------
-- 数据集 B：PUBLISHED 状态模型（已有 version=1 和 version=2，用于版本列表 / 切换 / Schema 查询测试）
-- -------------------------------------------------------

-- B1：插入 PUBLISHED 状态模型（current_version=2 表示当前生效版本）
INSERT INTO metadata_model (model_code, name, description, status, current_version, published_snapshot, gmt_create, gmt_modified)
VALUES (
    'test-published-{timestamp}',   -- model_code：语义主键
    'E2E已发布测试模型',
    '用于测试版本切换和 Schema 查询缓存的已发布模型',
    'PUBLISHED',                    -- 已发布状态
    2,                              -- 当前生效版本为 2
    'abc123def456abc123def456',     -- 模拟 published_snapshot 哈希值（24位十六进制）
    NOW(),
    NOW()
);

-- B2：为 PUBLISHED 模型插入 2 个字段（工作区字段，与 version=2 一致）
INSERT INTO metadata_field (model_id, field_name, field_type, required, sort_order, gmt_create, gmt_modified)
SELECT id, 'product_name', 'STRING', 1, 1, NOW(), NOW()
FROM metadata_model WHERE model_code = 'test-published-{timestamp}';

INSERT INTO metadata_field (model_id, field_name, field_type, required, sort_order, gmt_create, gmt_modified)
SELECT id, 'price', 'NUMBER', 1, 2, NOW(), NOW()
FROM metadata_model WHERE model_code = 'test-published-{timestamp}';

-- B3：插入 version=1 的版本记录（含 schema_content 缓存）
INSERT INTO metadata_model_version (model_code, version, schema_content, version_desc, gmt_create, gmt_modified)
VALUES (
    'test-published-{timestamp}',
    1,
    '{"$schema":"http://json-schema.org/draft-07/schema#","type":"object","properties":{"product_name":{"type":"string"}}}',
    '初始版本（仅含 product_name 字段）',
    DATE_SUB(NOW(), INTERVAL 1 HOUR),   -- version=1 早于 version=2 创建
    DATE_SUB(NOW(), INTERVAL 1 HOUR)
);

-- B4：插入 version=2 的版本记录（含 schema_content 缓存，新增了 price 字段）
INSERT INTO metadata_model_version (model_code, version, schema_content, version_desc, gmt_create, gmt_modified)
VALUES (
    'test-published-{timestamp}',
    2,
    '{"$schema":"http://json-schema.org/draft-07/schema#","type":"object","properties":{"product_name":{"type":"string"},"price":{"type":"number"}}}',
    '新增 price 字段',
    NOW(),
    NOW()
);

-- -------------------------------------------------------
-- 数据集 C：用于 API-4 用例 4-4 fallback 测试（schema_content 为空的版本记录）
-- -------------------------------------------------------

-- C1：插入一个 PUBLISHED 模型（已有 version=1，schema_content 为空，用于测试 fallback 实时计算）
INSERT INTO metadata_model (model_code, name, description, status, current_version, published_snapshot, gmt_create, gmt_modified)
VALUES (
    'test-fallback-{timestamp}',
    'Fallback测试模型',
    '用于测试 schema_content 为空时的 fallback 实时计算逻辑',
    'PUBLISHED',
    NULL,                            -- current_version 为 null（发布后未切换）
    'fallback123hash456fallback',    -- 模拟 published_snapshot
    NOW(),
    NOW()
);

-- C2：为 fallback 模型插入字段
INSERT INTO metadata_field (model_id, field_name, field_type, required, sort_order, gmt_create, gmt_modified)
SELECT id, 'fallback_field', 'STRING', 0, 1, NOW(), NOW()
FROM metadata_model WHERE model_code = 'test-fallback-{timestamp}';

-- C3：插入 schema_content 为 NULL 的版本记录（模拟历史遗留数据）
INSERT INTO metadata_model_version (model_code, version, schema_content, version_desc, gmt_create, gmt_modified)
VALUES (
    'test-fallback-{timestamp}',
    1,
    NULL,                            -- schema_content 为空，触发 fallback 逻辑
    '历史遗留版本，schema_content 为空',
    NOW(),
    NOW()
);

-- -------------------------------------------------------
-- 清理所有测试数据（测试完成后执行）
-- -------------------------------------------------------
-- DELETE FROM metadata_model_version WHERE model_code IN (
--     'test-draft-{timestamp}',
--     'test-published-{timestamp}',
--     'test-fallback-{timestamp}'
-- );
-- DELETE FROM metadata_field WHERE model_id IN (
--     SELECT id FROM metadata_model WHERE model_code IN (
--         'test-draft-{timestamp}',
--         'test-published-{timestamp}',
--         'test-fallback-{timestamp}'
--     )
-- );
-- DELETE FROM metadata_model WHERE model_code IN (
--     'test-draft-{timestamp}',
--     'test-published-{timestamp}',
--     'test-fallback-{timestamp}'
-- );
```

> **注意事项**
>
> 1. 执行前将所有 `{timestamp}` 替换为同一个 Unix 毫秒时间戳（可用 `date +%s%3N` 获取）
> 2. `{draftModelId}`、`{modelId}` 等占位符需在插入后执行 `SELECT id FROM metadata_model WHERE model_code = 'test-xxx-{timestamp}'` 获取实际 ID
> 3. `published_snapshot` 的模拟值格式应与实际 hash 算法输出长度一致（通常为 MD5 32位或 SHA-256 64位），本文件使用示意值，请根据实际实现调整
> 4. 端到端测试的每个场景使用独立的 `{timestamp}` 值，避免并行执行时数据冲突
