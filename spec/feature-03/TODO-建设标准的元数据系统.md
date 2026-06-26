# 建设标准的元数据系统 — 任务拆分

## 阶段一：基础准备（预估：3 人天）

### T-1 创建数据库表

- **描述**: 新建 `metadata_model`、`metadata_field`、`metadata_enum`、`metadata_enum_value` 四张表
- **涉及文件**: `basic_init.sql`
- **依赖**: 无
- **验收条件**:
  - [ ] 四张表创建成功，唯一索引和关联索引生效
  - [ ] 外键关系（model_id、enum_id）可正确关联
- **预估**: 0.5 人天

### T-2 创建 DO 实体与 MyBatis Mapper

- **描述**: 创建四张表对应的 DO 实体及 Mapper 接口/XML，支持 CRUD + 按 model_id 查字段列表 + 按 enum_id 查枚举值
- **涉及文件**: `stock-common/.../entity/`, `stock-common/.../mappers/`
- **依赖**: T-1
- **验收条件**:
  - [ ] 所有 DO 字段与数据库对应
  - [ ] Mapper 支持基础 CRUD + 按 model_id 查字段列表 + 按 enum_id 查枚举值
- **预估**: 1.5 人天

### T-3 创建领域模型与 Converter

- **描述**: 创建 `MetadataModel`、`MetadataField`、`MetadataEnum`、`MetadataEnumValue` 领域模型及转换器
- **涉及文件**: `stock-core/.../model/metadata/`, `stock-core/.../converter/MetadataConverter.java`
- **依赖**: T-2
- **验收条件**: 领域模型定义完整，DO ↔ Domain 转换正确
- **预估**: 1 人天

## 阶段二：核心功能（预估：6 人天）

### T-4 实现 MetadataDomainService

- **描述**: 实现元数据模型 CRUD、字段管理、枚举管理、Schema 校验
- **涉及文件**: `stock-core/.../service/MetadataDomainService.java`
- **依赖**: T-3
- **验收条件**:
  - [ ] 模型创建/更新/查询正确
  - [ ] 字段 CRUD 正确
  - [ ] 枚举管理（含枚举值）完整
  - [ ] Schema 校验返回明确的错误信息
- **预估**: 2.5 人天

### T-5 实现元数据管理后端 Controller

- **描述**: 实现模型管理、字段管理、枚举管理的所有 REST 接口，**包含枚举使用统计查询（`/api/metadata/enum/usage.json`），支持正向绑定和解除绑定**
- **涉及文件**: `stock-biz/.../controller/MetadataController.java`
- **依赖**: T-4
- **验收条件**:
  - [ ] 模型 list/detail/save 接口正常
  - [ ] 枚举 list（含引用数）/save/detail（含引用字段列表）接口正常
  - [ ] `GET /api/metadata/enum/usage.json` 返回枚举被哪些模型/字段引用
  - [ ] Schema 校验接口返回正确
  - [ ] 有引用时删除枚举返回错误提示
- **预估**: 2 人天

### T-6 实现本地集成 API

- **描述**: 实现指标含义查询和 AI 指标用法查询接口
- **涉及文件**: `stock-biz/.../controller/MetadataIntegrationController.java`
- **依赖**: T-4
- **验收条件**:
  - [ ] `GET /api/metadata/indicator/meaning` 返回完整字段定义 + 语义描述
  - [ ] `GET /api/metadata/indicator/usage` 返回业务逻辑 + 取数规则
- **预估**: 1 人天

### T-7 接入缓存层

- **描述**: 配置元数据全量缓存，写入时失效缓存
- **涉及文件**: `stock-core/.../service/impl/MetadataDomainServiceImpl.java`
- **依赖**: T-4
- **验收条件**: 缓存加载正确，写入后缓存失效，下次查询重新加载
- **预估**: 1 人天

## 阶段三：前端页面 + 完善（预估：5 人天）

### T-8 元数据模型列表与管理页面

- **描述**: 展示所有元数据模型，支持新建和编辑。在字段编辑器中，选择枚举时展示该枚举的引用热度（"此枚举还被其他 N 个字段使用"）
- **涉及文件**: `stock-frontend/src/pages/Metadata/ModelList/`, `stock-frontend/src/pages/Metadata/ModelEditor/`
- **依赖**: T-5
- **验收条件**:
  - [ ] 列表展示正确，新建/编辑表单可正常提交
  - [ ] 字段编辑器中引用枚举时显示引用热度提示
- **预估**: 2 人天

### T-9 枚举管理页面

- **描述**: 枚举定义管理页面，支持枚举值的增删改。列表页展示引用数（被 N 个模型、M 个字段引用），详情页展示引用字段列表（可跳转到对应模型），删除时做引用保护
- **涉及文件**: `stock-frontend/src/pages/Metadata/EnumManager/`
- **依赖**: T-5
- **验收条件**:
  - [ ] 列表展示正确，每行显示引用数
  - [ ] 枚举值增删改功能完整
  - [ ] 详情页展示引用字段列表，支持跳转到对应模型
  - [ ] 有引用时删除被阻止并提示
- **预估**: 2 人天

### T-10 模型详情展示页（Schema 展示）

- **描述**: 展示元数据模型的完整 Schema、字段列表、绑定的枚举，支持触发校验
- **涉及文件**: `stock-frontend/src/pages/Metadata/ModelDetail/`
- **依赖**: T-5
- **验收条件**: Schema 展示正确，校验结果可视化
- **预估**: 1 人天

### T-11 集成测试

- **描述**: 覆盖所有 API 端点的正向和异常场景
- **涉及文件**: `stock-web/.../MetadataIntegrationTest.java`
- **依赖**: T-5, T-6
- **验收条件**: `mvn test` 全部通过
- **预估**: 0.5 人天

## 工作量汇总

| 阶段 | 人天 |
|------|------|
| 阶段一：基础准备 | 3 天 |
| 阶段二：核心功能 | 6.5 天（↑0.5，枚举引用保护逻辑增加） |
| 阶段三：前端页面 + 完善 | 5.5 天（↑0.5，枚举引用展示交互增加） |
| **总计** | **15 天**（↑1，枚举多模型绑定相关） |
