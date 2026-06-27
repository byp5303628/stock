# 阶段一实现 — 基础准备（T-1 ~ T-3）

## 实现范围

### T-1: 创建数据库表
- 在 `basic_init.sql` 末尾追加 4 张表的 DDL：metadata_model、metadata_field、metadata_enum、metadata_enum_value
- DDL 定义见方案设计第 2.2 节

### T-2: 创建 DO 实体与 MyBatis Mapper
- 创建 4 个 DO 类：MetadataModelDO、MetadataFieldDO、MetadataEnumDO、MetadataEnumValueDO
- 创建 4 个 Mapper 接口：MetadataModelMapper、MetadataFieldMapper、MetadataEnumMapper、MetadataEnumValueMapper
- 创建 4 个 Mapper XML 文件
- 包路径: `com.ethanpark.stock.common.dal.mappers.entity` / `com.ethanpark.stock.common.dal.mappers`

### T-3: 创建领域模型与 Converter
- 创建 4 个领域模型：MetadataModel、MetadataField、MetadataEnum、MetadataEnumValue
- 在 DbConverter 中新增 DO→Domain 双向转换方法
- 在 DomainConverter 中新增 Domain←DO 转换方法

## 关键模式约束

### DO 类模式 (参考 TaskDO / ScheduleConfigDO)
- 使用 `@Data` 或 `@Getter @Setter` Lombok 注解
- 字段使用 camelCase，对应数据库 snake_case
- 日期字段使用 `java.util.Date` 类型
- 文件头 Javadoc 包含 @author baiyunpeng04

### Mapper 接口模式 (参考 TaskMapper)
- `@Mapper` 注解
- 方法：insert、updateById、selectById、selectAll（列表查询）
- MetadataFieldMapper 额外：selectByModelId、selectByEnumId、deleteById
- MetadataEnumValueMapper 额外：selectByEnumId、deleteByEnumId

### Mapper XML 模式 (参考 TaskMapper.xml)
- 使用 resultMap 映射
- insert 使用 useGeneratedKeys=true keyProperty=id
- selectById 使用参数化查询

### 领域模型模式 (参考 Task / ScheduleConfig)
- 使用 `@Getter @Setter` Lombok 注解
- 位于 `stock-core/src/main/java/com/ethanpark/stock/core/model/metadata/` 包

### Converter 模式 (参考 DbConverter / DomainConverter)
- 静态方法，双向转换
- JSON 字段使用 fastjson 的 `JSON.toJSONString()` / `JSON.parseObject()`
