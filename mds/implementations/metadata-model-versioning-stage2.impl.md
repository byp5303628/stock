# 阶段2 实现报告: Domain Service 层

## 文件变更

| 文件 | 操作 | 说明 |
|------|------|------|
| MetadataModel.java | 修改 | 新增 currentVersion, snapshotHash |
| MetadataModelVersion.java | 新增 | 版本领域模型 |
| DomainConverter.java | 修改 | 新增 version 转换 |
| DbConverter.java | 修改 | 新增 version 转换 |
| MetadataDomainService.java | 修改 | publishModel 签名变更, 新增4方法 |
| MetadataDomainServiceImpl.java | 修改 | 核心逻辑: 变更检测、发布、版本切换 |

## 编译状态
- stock-common: 通过
- stock-core: 通过
- stock-biz: 有编译错误（publishModel 签名变更，P3 修复）

## 核心逻辑
- computeModelHash: SHA-256 对模型属性+字段列表
- refreshModelStatus: saveModel/saveField 后调用
- publishModel: @Transactional 包含校验、版本生成、JSONSchema 缓存
