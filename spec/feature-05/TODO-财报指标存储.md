# 财报指标存储 — 任务拆分

## 阶段一：数据基础设施（预估：3 人天）

### T-1 创建数据库表和 DO 实体

- **描述**: 新建 `financial_report` 表及其对应的 DO 实体类
  - 在 `basic_init.sql` 中追加 `financial_report` 表的 CREATE TABLE DDL
  - 创建 `FinancialReportDO` 实体类（`stock-common/.../mappers/entity/`）
  - DO 字段与表字段一一对应，使用 `@Getter` `@Setter` Lombok 注解
  - 注意 `report_data` 字段为 String 类型（DO 层 JSON 文本），领域层转为 Map
- **涉及文件**:
  - `/Users/ethanpark/workspace/stock/basic_init.sql`（追加 DDL）
  - `stock-common/.../mappers/entity/FinancialReportDO.java`（**新增**）
- **依赖**: 无
- **验收条件**:
  - [ ] `basic_init.sql` 中有完整的 `financial_report` 表定义，包含唯一索引和所有业务索引
  - [ ] `FinancialReportDO` 类所有字段与表字段对齐
  - [ ] `report_type` 字段无 NOT NULL 冲突（DbConverter 中需提供默认值）
- **预估**: 0.5 人天

### T-2 创建 MyBatis Mapper

- **描述**: 创建 `FinancialReportMapper` 接口和 XML 映射文件，支持以下操作：
  - `insert` / `upsert`（INSERT ... ON DUPLICATE KEY UPDATE）
  - `updateById`
  - `selectById`
  - `selectPage` — 按 code, reportType, startDate, endDate, fiscalYear 分页查询
  - `count` — 分页计数
  - `selectLatestByCode` — 查询某股票最新各类型报表
  - `selectByCodeAndType` — 按 code + reportType 查询列表
  - `batchInsert` — 批量插入
- **涉及文件**:
  - `stock-common/.../mappers/FinancialReportMapper.java`（**新增**）
  - `stock-common/.../mappers/FinancialReportMapper.xml`（**新增**，在 resources 目录下）
- **依赖**: T-1
- **验收条件**:
  - [ ] 所有 Mapper 方法有正确的 SQL 实现
  - [ ] `upsert` 使用 ON DUPLICATE KEY UPDATE 实现幂等写入
  - [ ] `selectPage` 正确实现分页逻辑（LIMIT offset, size）
  - [ ] `count` 返回正确的总数
  - [ ] `selectLatestByCode` 对每种 reportType 只返回最新一条（GROUP BY + MAX）
- **预估**: 1 人天

### T-3 创建领域模型和 Converter

- **描述**: 
  - 创建 `FinancialReport` 领域对象（`stock-core/.../model/`），含 `Map<String, Object> reportData` 字段
  - 在 `DbConverter` 中增加 `toDbEntity(FinancialReport)` 方法：`reportData` 转为 JSON 字符串，NOT NULL 字段提供 null-safe 默认值
  - 在 `DomainConverter` 中增加 `toDomain(FinancialReportDO)` 方法：`reportData` JSON 字符串解析为 `Map<String, Object>`
  - 在 `DtoConverter` 中增加 `toDto(FinancialReport)` 方法
  - 创建请求 DTO：`FinancialReportSaveRequest`, `FinancialReportFetchRequest`, `BatchReportRequest`
  - 创建响应 DTO：`FinancialReportDTO`, `LatestReportsDTO`
- **涉及文件**:
  - `stock-core/.../model/FinancialReport.java`（**新增**）
  - `stock-core/.../converter/DbConverter.java`（**修改**）
  - `stock-core/.../converter/DomainConverter.java`（**修改**）
  - `stock-biz/.../converter/DtoConverter.java`（**修改**）
  - `stock-biz/.../dto/FinancialReportDTO.java`（**新增**）
  - `stock-biz/.../dto/FinancialReportSaveRequest.java`（**新增**）
  - `stock-biz/.../dto/FinancialReportFetchRequest.java`（**新增**）
  - `stock-biz/.../dto/BatchReportRequest.java`（**新增**）
  - `stock-biz/.../dto/LatestReportsDTO.java`（**新增**）
- **依赖**: T-1, T-2
- **验收条件**:
  - [ ] `DbConverter.toDbEntity(FinancialReport)` 中所有 NOT NULL 字段有 null-safe 默认值
  - [ ] `DomainConverter.toDomain(FinancialReportDO)` 正确解析 report_data JSON
  - [ ] `DtoConverter.toDto(FinancialReport)` 正确转换所有字段
  - [ ] 请求 DTO 使用 `@NotBlank`/`@NotNull` 等 Bean Validation 注解
- **预估**: 1.5 人天

## 阶段二：核心业务逻辑（预估：3 人天）

### T-4 实现 FinancialReportDomainService

- **描述**: 在 `stock-core` 中创建 `FinancialReportDomainService` 接口和实现类
  - `save(FinancialReport)` — 单条写入，调用 Mapper.upsert
  - `batchSave(List<FinancialReport>)` — 批量写入
  - `query(code, reportType, startDate, endDate, fiscalYear, page, size)` — 分页查询
  - `getById(Long id)` — 按 ID 查询
  - `getLatestByCode(String code, String reportType)` — 查询最新财报
  - `deleteById(Long id)` — 删除
  - 数据校验：code 不能为空，reportType 必须在允许的枚举值内，reportDate 格式校验
- **涉及文件**:
  - `stock-core/.../service/FinancialReportDomainService.java`（**新增**，接口）
  - `stock-core/.../service/impl/FinancialReportDomainServiceImpl.java`（**新增**，实现）
- **依赖**: T-3
- **验收条件**:
  - [ ] `save()` 幂等：重复提交相同 code+reportType+reportDate 不报错
  - [ ] `batchSave()` 支持批量提交，部分失败不影响其他记录
  - [ ] `query()` 分页结果正确，支持组合过滤条件
  - [ ] `getLatestByCode()` 正确返回每种报表类型的最新一条
  - [ ] `reportType` 校验：仅接受 CASH_FLOW / BALANCE / INCOME
  - [ ] 单元测试覆盖 save、batchSave、query、getLatestByCode 四个核心方法
- **预估**: 2 人天

### T-5 元数据模型初始化脚本

- **描述**: 
  - 通过元数据 API 创建三个财报模型（现金流量表、资产负债表、利润表）的初始化脚本/数据 SQL
  - 每个模型包含 2.2 节列出的所有字段定义
  - 可选的：创建 "报表类型" (report_type) 枚举，包含 CASH_FLOW/BALANCE/INCOME
  - 脚本应幂等（检查模型 code 是否已存在，已存在则跳过）
- **涉及文件**:
  - `init.sql` 或新的 `init_financial_metadata.sql`（**新增**，存放元数据预置数据）
  - 或通过 Java `@PostConstruct` 在启动时初始化（推荐，更可控）
- **依赖**: T-4（Service 已完成）
- **验收条件**:
  - [ ] 系统启动后，`GET /api/metadata/model/list.json` 能看到三个财报模型
  - [ ] 每个模型的字段定义正确，fieldType、businessMeaning 与设计文档一致
  - [ ] 脚本幂等：重复执行不报错，不产生重复记录
- **预估**: 1 人天

## 阶段三：外部数据源集成（预估：2 人天）

### T-6 实现 FinancialDataRemoteClient

- **描述**: 在 `stock-remote` 中创建财报数据源客户端
  - 调研并确定具体的公开数据源 endpoint（如腾讯/东方财富财报接口）
  - 使用 `Unirest` HTTP 客户端（与 `HistoryStockClient` 一致）
  - 实现 `fetchFinancialReport(code, reportType)` 方法
  - 实现外部字段名到内部字段名的映射（配置化或硬编码映射表）
  - 异常处理：网络超时、数据解析失败、空数据等
- **涉及文件**:
  - `stock-remote/.../FinancialDataRemoteClient.java`（**新增**）
  - `stock-remote/.../model/FinancialDataRaw.java`（**新增**，外部数据原始 DTO）
- **依赖**: T-4（需要 DomainService 落地数据）
- **验收条件**:
  - [ ] 通过单元测试 Mock HTTP 返回，验证 JSON 解析和字段映射正确
  - [ ] 异常场景（网络超时、空数据、格式错误）有对应处理逻辑
  - [ ] 字段映射表覆盖三张报表的核心指标字段
- **预估**: 2 人天

## 阶段四：REST API 层（预估：2 人天）

### T-7 实现 FinancialReportController

- **描述**: 创建 `FinancialReportController`，实现 2.5 节定义的所有接口
  - `GET /api/financial-reports` — 分页查询
  - `GET /api/financial-reports/detail` — 详情查询
  - `GET /api/financial-reports/latest` — 最新财报
  - `POST /api/financial-reports` — 单条创建/更新
  - `POST /api/financial-reports/batch` — 批量创建
  - `POST /api/financial-reports/fetch` — 触发外部数据拉取
  - `GET /api/financial-reports/models` — 元数据模型查询
  - 使用 `@Valid` 注解校验请求体
  - 异常通过 `GlobalExceptionHandler` 统一处理
  - 不使用 `Process Engine`（CRUD 场景不适用）
  - 类使用构造器注入，不使用 `@Resource` 字段注入
- **涉及文件**:
  - `stock-biz/.../controller/FinancialReportController.java`（**新增**）
- **依赖**: T-4, T-5, T-6
- **验收条件**:
  - [ ] 每个接口可通过 curl 调用并返回正确结果
  - [ ] 所有参数校验通过 `@Valid` + Bean Validation 实现
  - [ ] 错误场景返回正确的错误码和错误信息
  - [ ] 接口路径遵循 `/api/{resource-plural}` 规范，无 `.json` 后缀
  - [ ] 不需要 Cookie/Session，纯无状态 API
- **预估**: 2 人天

## 阶段五：测试（预估：2 人天）

### T-8 集成测试

- **描述**: 编写 FinancialReport 的集成测试，覆盖所有 API 端点
  - 使用 `SpringBootTest` + `TestRestTemplate` + H2 内存数据库
  - 遵循 `MetadataIT.java` 的测试模式（`@Nested` 场景分组）
  - 包含至少以下场景：
    - 财报数据 CRUD（创建、查询、详情、去重、更新）
    - 分页查询和组合筛选
    - 批量写入
    - 最新财报查询
    - 元数据模型查询
    - 参数校验错误
- **涉及文件**:
  - `stock-integration-test/.../FinancialReportIT.java`（**新增**）
- **依赖**: T-7
- **验收条件**:
  - [ ] 所有测试通过 `mvn test -pl stock-integration-test -am`
  - [ ] 覆盖正向场景和常见异常场景
  - [ ] 每个测试方法有 `@DisplayName` 描述
  - [ ] 场景独立，不互相依赖（使用 `@Nested` + `@BeforeEach` 初始化）
- **预估**: 1.5 人天

### T-9 单元测试

- **描述**: 编写核心逻辑的单元测试
  - `FinancialReportDomainService` 的 save/batchSave/query 方法
  - `DbConverter` 和 `DomainConverter` 的转换逻辑
  - `DtoConverter` 的 DTO 转换
  - `FinancialDataRemoteClient` 的字段映射逻辑
- **涉及文件**:
  - `stock-core/src/test/.../FinancialReportDomainServiceTest.java`（**新增**）
  - `stock-core/src/test/.../converter/DbConverterTest.java`（**修改**）
  - `stock-remote/src/test/.../FinancialDataRemoteClientTest.java`（**新增**）
- **依赖**: T-4, T-6
- **验收条件**:
  - [ ] 使用 Mockito Mock 掉 Mapper 依赖
  - [ ] 测试 save 的幂等性（重复提交不报错）
  - [ ] 测试 reportType 校验失败场景
  - [ ] 测试 Converter 的 null 安全处理
  - [ ] 覆盖率 > 80%
- **预估**: 1 人天

## 工作量汇总

| 阶段 | 内容 | 人天 |
|------|------|------|
| 阶段一 | 数据基础设施（表、DO、Mapper、领域模型、Converter） | 3 天 |
| 阶段二 | 核心业务逻辑（DomainService、元数据初始化） | 3 天 |
| 阶段三 | 外部数据源集成（RemoteClient） | 2 天 |
| 阶段四 | REST API 层（Controller） | 2 天 |
| 阶段五 | 测试（集成测试 + 单元测试） | 2 天 |
| **总计** | | **12 人天** |
