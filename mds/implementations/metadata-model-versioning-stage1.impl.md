# 阶段1 实现报告: DB + Mapper 层

## 文件变更

| 文件 | 操作 | 说明 |
|------|------|------|
| MetadataModelVersionDO.java | 新增 | 版本记录 DO |
| MetadataModelVersionMapper.java | 新增 | 版本 Mapper 接口 |
| MetadataModelVersionMapper.xml | 新增 | 版本 Mapper XML |
| MetadataModelDO.java | 修改 | 新增 currentVersion, snapshotHash |
| MetadataModelMapper.xml | 修改 | 新增列映射 |
| init-schema.sql | 修改 | H2 测试 schema |
| basic_init.sql | 修改 | 生产 DDL |

## 编译状态
- 编译通过
