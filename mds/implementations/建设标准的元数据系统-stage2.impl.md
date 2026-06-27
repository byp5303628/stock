# 阶段二实现 — 核心功能（T-4 ~ T-7）

## 实现范围

### T-4: 实现 MetadataDomainService
- 在 `stock-core/src/main/java/com/ethanpark/stock/core/service/` 创建 `MetadataDomainService.java`
- 遵循 `ScheduleConfigDomainService` / `TaskDomainService` 的模式
- 使用 `@Resource` 注入 4 个 Mapper（非构造器注入，遵循项目既有风格）
- 提供方法：
  - 模型管理: listModels(), getModelById(id), getModelByCode(code), saveModel(MetadataModel), deleteModel(id)
  - 字段管理: getFieldsByModelId(modelId), saveField(MetadataField), deleteField(id)
  - 枚举管理: listEnums(), getEnumById(id), getEnumByCode(code), saveEnum(MetadataEnum), deleteEnum(id)
  - 枚举值: getEnumValuesByEnumId(enumId), saveEnumValue(MetadataEnumValue), deleteEnumValue(id)
  - Schema 校验: validateSchema(modelId) → 返回 ValidationResult(boolean valid, List<ValidationError> errors)
  - 枚举引用查询: getEnumUsage(enumId) → 返回 EnumUsage(enumId, enumName, refModelCount, refFieldCount, List<RefDetail>)

### T-5: 实现元数据管理 Controller
- 在 `stock-biz/src/main/java/com/ethanpark/stock/biz/controller/` 创建 `MetadataController.java`
- 构造器注入 MetadataDomainService（Controller 遵循 api-design.md 规范用构造器注入）
- 提供接口（所有返回 `ResponseDTO<T>`）：
  - `GET /api/metadata/model/list.json` — 模型列表
  - `GET /api/metadata/model/detail.json?id=` — 模型详情（含字段列表）
  - `POST /api/metadata/model/save.json` — 新建/更新模型
  - `POST /api/metadata/model/validate.json` — Schema 校验（接收 {modelId} JSON body）
  - `POST /api/metadata/field/save.json` — 新增/更新字段
  - `DELETE /api/metadata/field/delete.json?id=` — 删除字段
  - `GET /api/metadata/enum/list.json` — 枚举列表（含各枚举的引用数）
  - `POST /api/metadata/enum/save.json` — 新建/更新枚举（含枚举值）
  - `GET /api/metadata/enum/detail.json?id=` — 枚举详情（含枚举值 + 引用字段列表）
  - `POST /api/metadata/enum/bind.json` — 绑定枚举到字段（接收 {fieldId, enumId}）
  - `POST /api/metadata/enum/unbind.json` — 解除枚举绑定（接收 {fieldId}）
  - `GET /api/metadata/enum/usage.json?id=` — 查询枚举被哪些模型/字段引用
- DTO 类位于 `stock-biz/src/main/java/com/ethanpark/stock/biz/dto/`：
  - MetadataModelDTO, MetadataFieldDTO, MetadataEnumDTO, MetadataEnumValueDTO
  - MetadataModelSaveRequest, MetadataFieldSaveRequest, MetadataEnumSaveRequest
  - EnumBindRequest, EnumUnbindRequest, ValidateRequest
  - ValidationResultDTO, ValidationErrorDTO, EnumUsageDTO, EnumRefDetailDTO
- 在 `stock-biz/.../converter/DtoConverter.java` 中新增 DTO ↔ Domain 转换方法
- 错误处理：通过 `throw new BusinessException(ErrorCode, message)` 抛异常，由 GlobalExceptionHandler 统一处理

### T-6: 实现本地集成 API
- 在 `stock-biz/src/main/java/com/ethanpark/stock/biz/controller/` 创建 `MetadataIntegrationController.java`
- 构造器注入 MetadataDomainService
- 接口：
  - `GET /api/metadata/indicator/meaning?code=` — 查询指标含义（返回完整字段定义 + 语义描述）
  - `GET /api/metadata/indicator/usage?code=` — 查询指标用法（返回业务逻辑 + 取数规则）

### T-7: 接入缓存层
- 在 `stock-core/pom.xml` 中添加 Caffeine 依赖
- 在 `stock-core/src/main/java/com/ethanpark/stock/core/config/StockCoreConfig.java` 中添加 `CacheManager` Bean 配置
- 在 MetadataDomainServiceImpl 中对 listModels()、listEnums() 使用 `@Cacheable`
- 对 save/delete 操作使用 `@CacheEvict` 使缓存失效
- 缓存 TTL 30 分钟

## 关键模式约束

### DomainService 模式（参考 ScheduleConfigDomainService）
- `@Service` 注解
- `@Resource` 注入 Mapper（遵循项目既有风格）
- 方法内：调用 Mapper → Converter 转换 → 返回领域对象
- save 方法内判断 id 是否为 null 决定 insert 还是 update（参考 TaskDomainService.save）

### Controller 模式（参考 ScheduleConfigController）
- `@RestController` + `@RequestMapping("/api/metadata")`
- 构造器注入（Controller 层遵循 api-design.md 规范）
- GET 方法用 `@RequestParam` 接收参数
- POST 方法用 `@RequestBody` 接收 JSON
- 返回 `ResponseDTO<T>`，成功用 `ResponseDTO.success(data)`，失败 throw BusinessException

### Controller 构造器注入模式
```java
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {
    private final MetadataDomainService metadataDomainService;

    public MetadataController(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }
    ...
}
```

### DTO 模式（参考 StrategyDTO / ScheduleConfigDTO）
- 使用 `@Getter @Setter` Lombok
- DTO 命名为 XxxDTO
- Request DTO 命名为 XxxRequest
- JSON 字段名使用 camelCase

### ErrorCode 扩展
- 在 ErrorCode.java 中新增元数据相关错误码（如 METADATA_MODEL_NOT_FOUND, METADATA_ENUM_NOT_FOUND 等）
- 保持与现有枚举风格一致

## 编译验证

完成后在项目根目录执行：
```bash
mvn compile -pl stock-core,stock-biz -q
```

要求编译零错误。如有编译错误，自行修复（最多 3 轮）。
